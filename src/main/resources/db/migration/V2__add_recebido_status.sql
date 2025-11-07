BEGIN;

ALTER TABLE public.correspondencias
  DROP CONSTRAINT IF EXISTS correspondencias_status_corresp_check;

ALTER TABLE public.correspondencias
  ADD CONSTRAINT correspondencias_status_corresp_check
  CHECK (status_corresp IN ('AVISADA','DEVOLVIDA','USO_INDEVIDO','ANALISE','RECEBIDO'));

COMMIT;
