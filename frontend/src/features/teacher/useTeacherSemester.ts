import { useQuery } from "@tanstack/react-query";
import { useEffect, useMemo, useState } from "react";
import { teacherApi } from "@/lib/api/teacher";
import { pickCurrentSemester } from "@/lib/semester";

export interface TeacherSemesterOption {
  id: string;
  name: string;
}

function mapApiSemesterOptions(
  data: Awaited<ReturnType<typeof teacherApi.listSemesters>>,
): TeacherSemesterOption[] {
  return data.map((semester) => ({
    id: String(semester.id),
    name: semester.name,
  }));
}

export function useTeacherSemester() {
  const semestersQuery = useQuery({
    queryKey: ["teacher", "semesters"],
    queryFn: teacherApi.listSemesters,
    retry: false,
  });

  const defaultSemesterId = useMemo(() => {
    const current = pickCurrentSemester(semestersQuery.data ?? []);
    return current ? String(current.id) : "";
  }, [semestersQuery.data]);

  const [semesterId, setSemesterId] = useState("");

  useEffect(() => {
    if (!semesterId && defaultSemesterId) setSemesterId(defaultSemesterId);
  }, [defaultSemesterId, semesterId]);

  const semesterOptions = useMemo(
    () =>
      semestersQuery.data?.length && !semestersQuery.isError
        ? mapApiSemesterOptions(semestersQuery.data)
        : [],
    [semestersQuery.data, semestersQuery.isError],
  );

  return {
    semesterId,
    setSemesterId,
    semesterOptions,
    semestersQuery,
  };
}
