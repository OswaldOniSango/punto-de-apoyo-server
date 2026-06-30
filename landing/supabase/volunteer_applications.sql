CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS public.volunteer_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name TEXT NOT NULL,
    country TEXT NOT NULL,
    city TEXT NOT NULL,
    email TEXT NOT NULL,
    contact TEXT NOT NULL,
    timezone TEXT NOT NULL,
    weekly_availability TEXT NOT NULL,
    start_availability TEXT NOT NULL,
    primary_profile TEXT NOT NULL,
    secondary_profiles JSONB NOT NULL DEFAULT '[]'::JSONB,
    professional_experience TEXT NOT NULL,
    production_experience TEXT NOT NULL,
    open_source_experience TEXT NOT NULL,
    remote_experience TEXT NOT NULL,
    relevant_experience TEXT NOT NULL,
    backend_skills JSONB NOT NULL DEFAULT '[]'::JSONB,
    frontend_skills JSONB NOT NULL DEFAULT '[]'::JSONB,
    qa_skills JSONB NOT NULL DEFAULT '[]'::JSONB,
    devops_skills JSONB NOT NULL DEFAULT '[]'::JSONB,
    maps_data_skills JSONB NOT NULL DEFAULT '[]'::JSONB,
    initial_participation TEXT NOT NULL,
    accepts_task TEXT NOT NULL,
    meetings_availability TEXT NOT NULL,
    available_schedule JSONB NOT NULL DEFAULT '[]'::JSONB,
    github_url TEXT,
    linkedin_url TEXT,
    portfolio_url TEXT,
    cv_url TEXT,
    comments TEXT,
    consent BOOLEAN NOT NULL,
    source TEXT NOT NULL DEFAULT 'landing',
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE public.volunteer_applications ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Public can create volunteer applications" ON public.volunteer_applications;

CREATE POLICY "Public can create volunteer applications"
ON public.volunteer_applications
FOR INSERT
TO anon
WITH CHECK (
    consent = true
    AND length(trim(full_name)) > 1
    AND email LIKE '%@%'
);
