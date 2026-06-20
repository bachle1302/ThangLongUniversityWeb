import { useForm } from "react-hook-form";
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { ClassSectionFormValues } from "./types";

export type ClassSectionSelectOption = { value: number | string; label: string };

export function ClassSectionSelectField({
  control,
  name,
  label,
  options,
}: {
  control: ReturnType<typeof useForm<ClassSectionFormValues>>["control"];
  name: keyof ClassSectionFormValues;
  label: string;
  options: ClassSectionSelectOption[];
}) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem>
          <FormLabel>{label}</FormLabel>
          <Select
            value={String(field.value)}
            onValueChange={(value) => field.onChange(toFieldValue(value, options))}
          >
            <FormControl>
              <SelectTrigger>
                <SelectValue placeholder={label} />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              {options.map((option) => (
                <SelectItem key={String(option.value)} value={String(option.value)}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

function toFieldValue(value: string, options: ClassSectionSelectOption[]) {
  const option = options.find((item) => String(item.value) === value);
  return typeof option?.value === "number" ? Number(value) : value;
}
