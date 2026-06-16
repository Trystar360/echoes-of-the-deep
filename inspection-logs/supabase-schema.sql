-- Spruce Lake Adventure — cloud sync schema
-- Run once in your Supabase project's SQL editor.

create table if not exists inspection_logs (
  id            text primary key,
  template_id   text,
  template_name text,
  meta          jsonb,
  results       jsonb,
  notes         text,
  deleted       boolean default false,
  created_at    timestamptz,
  updated_at    timestamptz
);

create index if not exists inspection_logs_updated_at
  on inspection_logs (updated_at);

-- Row Level Security.
-- The policy below grants the public (anon) key full access — appropriate for a
-- single trusted team or kiosk device. Tighten (e.g. require auth) for wider use.
alter table inspection_logs enable row level security;

create policy "team access" on inspection_logs
  for all using (true) with check (true);
