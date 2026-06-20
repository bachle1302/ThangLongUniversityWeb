import { useQueryClient } from "@tanstack/react-query";
import { useEffect } from "react";
import { API_BASE_URL, getStoredAuth } from "@/lib/api/client";

export function useSemesterRealtime(semesterId: number | null | undefined) {
  const queryClient = useQueryClient();

  useEffect(() => {
    if (semesterId == null || !Number.isFinite(semesterId)) return;

    const controller = new AbortController();
    let reconnectTimer: number | undefined;
    let invalidateTimer: number | undefined;

    const invalidateSemesterData = () => {
      window.clearTimeout(invalidateTimer);
      invalidateTimer = window.setTimeout(() => {
        void queryClient.invalidateQueries({ queryKey: ["admin", "semester-summary", semesterId] });
        void queryClient.invalidateQueries({ queryKey: ["admin", "dashboard"] });
        void queryClient.invalidateQueries({ queryKey: ["admin", "registration-rounds", semesterId] });
        void queryClient.invalidateQueries({
          queryKey: ["admin", "class-sections", "semester", semesterId],
        });
        void queryClient.invalidateQueries({ queryKey: ["admin", "enrollments", semesterId] });
        void queryClient.invalidateQueries({ queryKey: ["admin", "exam-registrations", semesterId] });
        void queryClient.invalidateQueries({ queryKey: ["admin", "semesters"] });
        void queryClient.invalidateQueries({ queryKey: ["student", "course-registration-overview"] });
        void queryClient.invalidateQueries({ queryKey: ["student", "selected-enrollments", semesterId] });
        void queryClient.invalidateQueries({ queryKey: ["student", "schedule", semesterId] });
        void queryClient.invalidateQueries({ queryKey: ["student", "retakes"] });
        void queryClient.invalidateQueries({ queryKey: ["student", "dashboard"] });
      }, 100);
    };

    const connect = async () => {
      const token = getStoredAuth()?.accessToken;
      if (!token || controller.signal.aborted) return;

      try {
        const response = await fetch(`${API_BASE_URL}/api/realtime/semesters/${semesterId}`, {
          headers: {
            Accept: "text/event-stream",
            Authorization: `Bearer ${token}`,
          },
          signal: controller.signal,
        });
        if (!response.ok || !response.body) {
          throw new Error(`Realtime connection failed (${response.status})`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = "";
        while (!controller.signal.aborted) {
          const { value, done } = await reader.read();
          if (done) break;
          buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, "\n");
          const events = buffer.split("\n\n");
          buffer = events.pop() ?? "";
          for (const event of events) {
            if (event.includes("event:semester-update")) {
              invalidateSemesterData();
            }
          }
        }
      } catch (error) {
        if (controller.signal.aborted) return;
        console.warn("Semester realtime disconnected, reconnecting.", error);
      }

      if (!controller.signal.aborted) {
        reconnectTimer = window.setTimeout(() => void connect(), 3000);
      }
    };

    void connect();
    const fallbackTimer = window.setInterval(invalidateSemesterData, 30_000);

    return () => {
      controller.abort();
      window.clearTimeout(reconnectTimer);
      window.clearTimeout(invalidateTimer);
      window.clearInterval(fallbackTimer);
    };
  }, [queryClient, semesterId]);
}
