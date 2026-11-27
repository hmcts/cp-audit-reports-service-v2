# Audit workbook bundle

The existing report download continues to serve the pseudonymised workbook.
`GET /reports/bundle?reference=<audit report reference>` returns an attachment
with the two workbooks for the same completed Fabric run. No pipeline definition
or output path changes are needed. Legacy reports without this pair are not
eligible. The combined workbook size limit is 32 MiB.

The route uses the same authorisation as the report listing: membership in
Auditors and the existing Crime Platform Audit create permission. No separate
identity-key permission is required. The BFF identity needs scoped read access
to the private internal-audit-reports container; auditors do not need direct
storage access. No identity-key link is added to the listing or issued to the
browser.

Before enabling this in any environment, confirm the gateway routes the endpoint
through the standard authentication and authorisation stack. In STE, use
synthetic workbooks and prove that an auditor can download the original report
or one ZIP with both workbooks, and that a caller without report-listing access
gets a 403 for the bundle. Verify that the successful download is logged and
that a missing key workbook returns no ZIP.
