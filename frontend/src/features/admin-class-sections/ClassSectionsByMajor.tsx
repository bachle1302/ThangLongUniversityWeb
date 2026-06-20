import { ClassSectionsTable } from "./ClassSectionsTable";
import type { ClassSectionRow } from "./types";

interface ClassSectionsByMajorProps {
  rows: ClassSectionRow[];
  onEdit: (row: ClassSectionRow) => void;
  onDelete: (row: ClassSectionRow) => void;
  onViewStudents: (row: ClassSectionRow) => void;
}

export function ClassSectionsByMajor({
  rows,
  onEdit,
  onDelete,
  onViewStudents,
}: ClassSectionsByMajorProps) {
  const groups = groupByMajor(rows);

  return (
    <div className="space-y-6">
      {groups.map((group) => (
        <ClassSectionsTable
          key={group.majorName}
          title={group.majorName}
          rows={group.rows}
          onEdit={onEdit}
          onDelete={onDelete}
          onViewStudents={onViewStudents}
        />
      ))}
    </div>
  );
}

function groupByMajor(rows: ClassSectionRow[]) {
  const groups = new Map<string, ClassSectionRow[]>();
  rows.forEach((row) => {
    const key = row.majorName || "Chưa phân ngành";
    groups.set(key, [...(groups.get(key) ?? []), row]);
  });

  return Array.from(groups.entries())
    .map(([majorName, groupRows]) => ({ majorName, rows: groupRows }))
    .sort((left, right) => left.majorName.localeCompare(right.majorName));
}
