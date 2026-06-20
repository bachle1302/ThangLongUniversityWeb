package com.example.ThangLongUniversityWeb.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits document text into overlapping chunks suitable for embedding.
 *
 * Strategy:
 *  1. Split by Markdown headings (## / ###) first.
 *  2. If a section exceeds maxChunkChars, split further at paragraph boundaries (\n\n).
 *  3. Apply an overlap of overlapChars characters (copied from the tail of the previous chunk).
 */
@Component
public class TextChunker {

    private static final int DEFAULT_MAX_CHARS  = 800;
    private static final int DEFAULT_OVERLAP     = 120;

    public List<String> chunk(String text) {
        return chunk(text, DEFAULT_MAX_CHARS, DEFAULT_OVERLAP);
    }

    public List<String> chunk(String text, int maxChars, int overlapChars) {
        if (text == null || text.isBlank()) return List.of();

        // 1. Split by headings
        List<String> sections = splitByHeadings(text);

        // 2. Further split large sections by paragraph
        List<String> rawChunks = new ArrayList<>();
        for (String section : sections) {
            if (section.length() <= maxChars) {
                rawChunks.add(section.strip());
            } else {
                rawChunks.addAll(splitByParagraph(section, maxChars));
            }
        }

        // 3. Apply overlap
        return applyOverlap(rawChunks, overlapChars);
    }

    private List<String> splitByHeadings(String text) {
        // Split on lines that start with ## or ###
        String[] lines = text.split("\n");
        List<String> sections = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String line : lines) {
            if ((line.startsWith("## ") || line.startsWith("### ")) && !current.isEmpty()) {
                sections.add(current.toString().strip());
                current = new StringBuilder();
            }
            current.append(line).append("\n");
        }
        if (!current.isEmpty()) {
            sections.add(current.toString().strip());
        }
        return sections.isEmpty() ? List.of(text) : sections;
    }

    private List<String> splitByParagraph(String text, int maxChars) {
        String[] paragraphs = text.split("\n\n+");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String para : paragraphs) {
            if (current.length() + para.length() + 2 > maxChars && !current.isEmpty()) {
                chunks.add(current.toString().strip());
                current = new StringBuilder();
            }
            current.append(para).append("\n\n");
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString().strip());
        }
        return chunks.isEmpty() ? List.of(text) : chunks;
    }

    private List<String> applyOverlap(List<String> chunks, int overlapChars) {
        if (chunks.size() <= 1 || overlapChars <= 0) return chunks;
        List<String> result = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            if (i == 0) {
                result.add(chunks.get(i));
            } else {
                String prev = chunks.get(i - 1);
                String overlap = prev.length() > overlapChars
                        ? prev.substring(prev.length() - overlapChars)
                        : prev;
                result.add(overlap + "\n" + chunks.get(i));
            }
        }
        return result;
    }
}
