import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { PageHeader } from "@/components/ui/page-header";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { teacherApi } from "@/lib/api/teacher";
import {
  BookOpen,
  Building2,
  CalendarDays,
  Contact,
  Home,
  IdCard,
  Mail,
  MapPin,
  Phone,
  ShieldCheck,
  UserRound,
  Users,
} from "lucide-react";
import type { ComponentType } from "react";

export const Route = createFileRoute("/teacher/profile")({
  component: TeacherProfilePage,
});

interface DisplayField {
  label: string;
  value: string | number;
}

function TeacherProfilePage() {
  const profileQuery = useQuery({
    queryKey: ["teacher", "profile"],
    queryFn: teacherApi.getProfile,
    retry: false,
  });

  if (profileQuery.isPending) {
    return <ProfileSkeleton />;
  }

  if (profileQuery.isError) {
    return (
      <div>
        <PageHeader title="Thông tin cá nhân" description="Thông tin giảng viên và liên hệ" />
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          {profileQuery.error instanceof Error
            ? profileQuery.error.message
            : "Không tải được hồ sơ giảng viên"}
        </div>
      </div>
    );
  }

  const profile = profileQuery.data;
  const fullName = profile.fullName ?? profile.username;
  const teacherCode = profile?.code ?? "-";
  const degree = profile?.majorOrDegree ?? "-";
  const initials = fullName
    .split(" ")
    .filter(Boolean)
    .slice(-2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();

  const val = (v: string | number | null | undefined) => (v != null && v !== "" ? v : "-");

  const identityFields: DisplayField[] = [
    { label: "Họ và tên", value: val(profile?.fullName) },
    { label: "Tên đăng nhập", value: val(profile?.username) },
    { label: "Mã giảng viên", value: val(profile?.code) },
    { label: "Vai trò", value: val(profile?.role) },
    { label: "Giới tính", value: val(profile?.gender) },
    { label: "Ngày sinh", value: val(profile?.dateOfBirth) },
    { label: "Tuổi", value: val(profile?.age) },
    { label: "CCCD", value: val(profile?.nationalId) },
  ];

  const contactFields: DisplayField[] = [
    { label: "Email", value: val(profile?.email) },
    { label: "Số điện thoại", value: val(profile?.phone) },
    { label: "Nơi sinh", value: val(profile?.placeOfBirth) },
    { label: "Quê quán", value: val(profile?.hometown) },
    { label: "Địa chỉ thường trú", value: val(profile?.permanentAddress) },
    { label: "Nơi ở hiện tại", value: val(profile?.currentAddress) },
    { label: "Liên hệ khẩn cấp", value: val(profile?.emergencyContact) },
  ];

  const professionalFields: DisplayField[] = [
    { label: "Khoa/Bộ môn", value: val(profile?.department) },
    { label: "Học vị", value: val(degree) },
  ];

  return (
    <div>
      <PageHeader title="Thông tin cá nhân" description="Thông tin giảng viên và liên hệ" />

      <div className="grid gap-5 xl:grid-cols-[320px_minmax(0,1fr)]">
        <Card className="overflow-hidden">
          <div className="bg-primary px-6 py-8 text-primary-foreground">
            <Avatar className="mx-auto h-24 w-24 border-4 border-primary-foreground/40">
              {profile?.avatarUrl && <AvatarImage src={profile.avatarUrl} alt={fullName} />}
              <AvatarFallback className="bg-primary-foreground text-2xl text-primary">
                {initials || "GV"}
              </AvatarFallback>
            </Avatar>
            <div className="mt-4 text-center">
              <div className="text-xl font-semibold">{fullName}</div>
              <div className="mt-1 font-mono text-sm opacity-85">{teacherCode}</div>
            </div>
          </div>

          <div className="space-y-4 p-5">
            <div className="flex flex-wrap justify-center gap-2">
              {degree !== "-" && <Badge>{degree}</Badge>}
              {profile?.department && <Badge variant="secondary">{profile.department}</Badge>}
            </div>
          </div>
        </Card>

        <div className="space-y-5">
          <InfoSection
            title="Thông tin định danh"
            description="Thông tin hồ sơ cá nhân và giấy tờ giảng viên"
            icon={IdCard}
            fields={identityFields}
          />
          <InfoSection
            title="Liên hệ và cư trú"
            description="Thông tin liên lạc, quê quán và địa chỉ hiện tại"
            icon={MapPin}
            fields={contactFields}
          />
          <InfoSection
            title="Thông tin chuyên môn"
            description="Học vị, khoa bộ môn và chuyên ngành giảng dạy"
            icon={BookOpen}
            fields={professionalFields}
          />
        </div>
      </div>
    </div>
  );
}

function InfoSection({
  title,
  description,
  icon: Icon,
  fields,
}: {
  title: string;
  description: string;
  icon: ComponentType<{ className?: string }>;
  fields: DisplayField[];
}) {
  return (
    <Card className="p-5">
      <div className="flex items-start gap-3">
        <div className="grid h-10 w-10 place-items-center rounded-lg bg-primary/10 text-primary">
          <Icon className="h-5 w-5" />
        </div>
        <div>
          <h2 className="text-base font-semibold">{title}</h2>
          <p className="mt-1 text-sm text-muted-foreground">{description}</p>
        </div>
      </div>

      <div className="mt-5 grid gap-3 md:grid-cols-2">
        {fields.map((item) => (
          <ProfileField key={item.label} field={item} icon={iconForLabel(item.label)} />
        ))}
      </div>
    </Card>
  );
}

function ProfileSkeleton() {
  return (
    <div>
      <PageHeader title="Thông tin cá nhân" description="Đang tải hồ sơ giảng viên" />
      <div className="grid gap-5 xl:grid-cols-[320px_minmax(0,1fr)]">
        <Card className="p-5">
          <Skeleton className="mx-auto h-24 w-24 rounded-full" />
          <Skeleton className="mx-auto mt-4 h-5 w-40" />
          <Skeleton className="mx-auto mt-2 h-4 w-24" />
        </Card>
        <div className="space-y-5">
          {[0, 1, 2].map((section) => (
            <Card key={section} className="p-5">
              <Skeleton className="h-5 w-48" />
              <div className="mt-5 grid gap-3 md:grid-cols-2">
                {[0, 1, 2, 3].map((field) => (
                  <Skeleton key={field} className="h-20" />
                ))}
              </div>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}

function ProfileField({
  field: item,
  icon: Icon,
}: {
  field: DisplayField;
  icon: ComponentType<{ className?: string }>;
}) {
  return (
    <div className="min-h-20 rounded-lg border bg-muted/20 p-4">
      <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-muted-foreground">
        <Icon className="h-4 w-4" />
        {item.label}
      </div>
      <div className="mt-2 break-words text-sm font-semibold leading-6">{item.value}</div>
    </div>
  );
}

function iconForLabel(label: string): ComponentType<{ className?: string }> {
  const map: Record<string, ComponentType<{ className?: string }>> = {
    "Họ và tên": UserRound,
    "Tên đăng nhập": Contact,
    "Mã giảng viên": IdCard,
    "Vai trò": ShieldCheck,
    "Giới tính": Users,
    "Ngày sinh": CalendarDays,
    Tuổi: CalendarDays,
    CCCD: IdCard,
    Email: Mail,
    "Số điện thoại": Phone,
    "Nơi sinh": MapPin,
    "Quê quán": Home,
    "Địa chỉ thường trú": Home,
    "Nơi ở hiện tại": MapPin,
    "Liên hệ khẩn cấp": Phone,
    "Khoa/Bộ môn": Building2,
    "Học vị": BookOpen,
  };
  return map[label] ?? UserRound;
}
