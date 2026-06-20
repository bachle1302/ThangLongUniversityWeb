package com.example.ThangLongUniversityWeb.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleUtils {

    // Hàm kiểm tra 2 chuỗi lịch học có đụng nhau không. Trả về true nếu TRÙNG.
    public static boolean isOverlap(String schedule1, String schedule2) {
        if (schedule1 == null || schedule2 == null || schedule1.isBlank() || schedule2.isBlank()) {
            return false; // Lớp chưa có lịch thì không sợ trùng
        }

        // Tách chuỗi thành các cụm. VD: "T2(1-3), T5(4-6)" -> ["T2(1-3)", " T5(4-6)"]
        String[] parts1 = schedule1.split(",");
        String[] parts2 = schedule2.split(",");

        for (String p1 : parts1) {
            for (String p2 : parts2) {
                if (checkOverlapSingle(p1.trim(), p2.trim())) {
                    return true; // Chỉ cần 1 ngày đụng nhau là toang cả lịch
                }
            }
        }
        return false;
    }

    // Kiểm tra 2 cụm đơn lẻ. VD: T2(1-3) và T2(3-5)
    private static boolean checkOverlapSingle(String p1, String p2) {
        // Dùng Regex để bóc tách: Nhóm 1 (Thứ), Nhóm 2 (Tiết bắt đầu), Nhóm 3 (Tiết kết thúc)
        Pattern pattern = Pattern.compile("T(\\d+)\\((\\d+)-(\\d+)\\)");
        Matcher m1 = pattern.matcher(p1);
        Matcher m2 = pattern.matcher(p2);

        if (m1.find() && m2.find()) {
            int day1 = Integer.parseInt(m1.group(1));
            int start1 = Integer.parseInt(m1.group(2));
            int end1 = Integer.parseInt(m1.group(3));

            int day2 = Integer.parseInt(m2.group(1));
            int start2 = Integer.parseInt(m2.group(2));
            int end2 = Integer.parseInt(m2.group(3));

            // Nếu khác ngày (VD Thứ 2 và Thứ 3) -> Không trùng
            if (day1 != day2) {
                return false;
            }

            // Nếu cùng ngày -> Kiểm tra tiết học có đè lên nhau không
            // Công thức chống đè: Bắt đầu 1 <= Kết thúc 2 VÀ Kết thúc 1 >= Bắt đầu 2
            return (start1 <= end2) && (end1 >= start2);
        }
        return false;
    }
}