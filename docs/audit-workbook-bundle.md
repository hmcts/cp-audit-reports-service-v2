# Audit workbook bundle

The existing report download continues to serve the pseudonymised workbook.
`GET /reports/bundle?reference=<audit report reference>` returns an attachment
with the two workbooks for the same completed Fabric run. No pipeline definition
or output path changes are needed. Legacy reports without this pair are not
eligible. The combined workbook size limit is 32 MiB.

The route requires membership in Auditors, the existing Crime Platform Audit
create permission, and the untargeted Crime Platform Audit Identity Key
download permission. Grant the new permission only to approved recipients of
both workbooks; do not add it to the baseline auditor role. The BFF identity
needs scoped read access to the private internal-audit-reports container.
No identity-key link is added to the listing or issued to the browser.

Before enabling this in any environment, confirm the gateway routes the endpoint
through the standard authentication and authorisation stack. In STE, use
synthetic workbooks and prove that a normal auditor still downloads the original
report but gets a 403 on the bundle endpoint, and that a permitted auditor gets
one ZIP with both workbooks. Verify that the successful download is logged and
that a missing key workbook returns no ZIP.
