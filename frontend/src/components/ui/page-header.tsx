import type { ReactNode } from "react";

export function PageHeader({
  title,
  description,
  actions,
}: {
  title: string;
  description?: string;
  actions?: ReactNode;
}) {
  return (
    <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
        {description && <p className="mt-1 text-sm text-muted-foreground">{description}</p>}
      </div>
      {actions && <div className="flex flex-wrap items-center gap-2">{actions}</div>}
    </div>
  );
}

export function StatCard({
  label,
  value,
  hint,
  icon: Icon,
  tone = "primary",
}: {
  label: string;
  value: string | number;
  hint?: string;
  icon?: React.ComponentType<{ className?: string }>;
  tone?: "primary" | "success" | "warning" | "info" | "destructive";
}) {
  const toneClass = {
    primary: "from-primary/10 to-primary/0 text-primary",
    success: "from-success/15 to-success/0 text-success",
    warning: "from-warning/20 to-warning/0 text-warning-foreground",
    info: "from-info/15 to-info/0 text-info",
    destructive: "from-destructive/15 to-destructive/0 text-destructive",
  }[tone];
  return (
    <div className="relative overflow-hidden rounded-xl border bg-card p-5 shadow-sm">
      <div
        className={`pointer-events-none absolute -right-8 -top-8 h-32 w-32 rounded-full bg-gradient-to-br ${toneClass} blur-2xl`}
      />
      <div className="relative flex items-start justify-between">
        <div>
          <div className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
            {label}
          </div>
          <div className="mt-2 text-2xl font-semibold tabular-nums">{value}</div>
          {hint && <div className="mt-1 text-xs text-muted-foreground">{hint}</div>}
        </div>
        {Icon && (
          <div
            className={`grid h-10 w-10 place-items-center rounded-lg bg-gradient-to-br ${toneClass}`}
          >
            <Icon className="h-5 w-5" />
          </div>
        )}
      </div>
    </div>
  );
}
