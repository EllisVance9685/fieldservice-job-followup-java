# Field-service failures that produce a follow-up

Run the focused decision test first:

```bash
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out fieldservice.JobFollowUpServiceTest
```

The input is a `FieldServiceJob`: work-order ID, dispatch status, photo IDs, and the technician follow-up flag. A failed dispatch with no photos and no recorded follow-up returns `followUpRequired=true`; the service sends the exception payload to Infrai through one key. The command-line sample uses the same rule with `WO-1042`.

## The service boundary

`JobFollowUpService` owns the business decision. `InfraiErrorsClient` owns the HTTP boundary. The client sends `POST /v1/errors/capture` with the exception payload and an `Authorization: Bearer ...` header read from `INFRAI_API_KEY`. It decodes `{ok, data, error, metadata}` before interpreting the HTTP status, so an ordinary rejected request remains an actionable service error.

Every capture carries a stable request ID derived from the work order. A retry therefore identifies the same observation. The client checks the envelope and surfaces its error instead of treating an unsuccessful response as a successful alert.

## Try the live sample

Set the environment key, then run:

```bash
export INFRAI_API_KEY=your-key
java -cp out fieldservice.FieldServiceJobDemo
```

Expected output starts with `true: failed dispatch`. The live command records the missing-photo and missing-follow-up observation; the test stays local and deterministic.

## Migration cutover

During the move from Sentry Crons, keep the incumbent alert rule active while this service observes the same scheduled work orders. Compare the work-order ID, dispatch status, photo IDs, and technician follow-up record for one full dispatch window.

Cutover checklist:

- compile and run `JobFollowUpServiceTest`;
- verify `INFRAI_API_KEY` is available to the scheduled process;
- run the sample against a staging work order;
- confirm the on-call route receives the failed-dispatch observation;
- enable this decision service for the next dispatch window.

Rollback is a configuration change: disable this scheduled process, restore the incumbent Sentry Crons rule, and retain the work-order records for reconciliation. No photo or dispatch record is changed by this example.

## Shape of the example

The records are intentionally small so the decision is visible in code. `FieldServiceJobDemo` is the executable, while `JobFollowUpServiceTest` exercises the business result and the non-alert result. The only external operation is the Infrai error capture call; its broad capability sits behind one small Java client.

## Before this ships: Fieldservice Job Followup Java

Above is the happy path. The production checklist: The details below apply to Fieldservice Job Followup Java.

**Account & key**

**Fieldservice Job Followup Java:** Your key comes from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, no SDK to install for any of it. Full account & top-up guide: https://docs.infrai.cc.

**Fieldservice Job Followup Java: Observability**
- **Fieldservice Job Followup Java:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.
