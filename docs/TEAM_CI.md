# Team Branch Builds And Publication

This change must be merged into the relevant team branch: fixes present only on
main do not update a team's saved workflow definitions.

| Event | Build/test | Maven publication | ACR image | Live ACR promotion |
| --- | --- | --- | --- | --- |
| Push to team/** | Yes | Yes | Non-live | No |
| Same-repository PR targeting team/** | Yes | Yes | Non-live | No |
| PR targeting main/master | Yes | No | No | No |
| Fork PR | Build checks only | No | No | No |
| Push to main/master | Yes | Yes | Existing path | Existing path |

The caller branch filters apply before the publication expressions. This is a
same-repository team development route, not a pull_request_target workflow.
Review workflow changes before granting publication credentials to contributors.

Non-release versions include the run ID and attempt; PR versions additionally
include the PR number. Use the exact coordinates in the successful publication
job summary, not an old expected snapshot version. A successful build with a
skipped publish job is not evidence of publication.

## Destinations

- Maven: `uk.gov.hmcts.cp:cp-audit-reports-service-v2:<version>` in the Azure
  Artifacts `hmcts-lib` feed and GitHub Packages. The Gradle configuration does
  not directly publish to JFrog Artifactory.
- ACR build: [cp-gh-artifact-to-acr, definition 460](https://dev.azure.com/hmcts-cpp/cpp-apps/_build?definitionId=460).
  The workflow supplies the source commit and sets `pushToLive=false` for team
  events. The shared pipeline reports the actual image tag, which includes its
  date suffix; use that tag rather than the Maven version alone.
- Application deploy: [CPP-AKS-DEPLOY, definition 333](https://dev.azure.com/hmcts-cpp/cpp-apps/_build?definitionId=333).
  This is separate from image publication. The application release is
  `auditreports-service`, and its deployment image setting is
  `auditreports_service_image_tag`. Select the approved environment, stack and
  version configuration before a separately authorised deployment.

The reusable workflow retains the legacy `trigger_deploy` input name, but that
job only queues the ACR build. It does not deploy the application to AKS. Main
and release registry promotion behaviour is unchanged; team builds never request
the live registry stage.

## Checks And Remaining Risks

Publishing requires the existing Azure Artifacts credentials and the ADO trigger
PAT. Missing Maven credentials now fail before publication without printing them.
Successful GitHub queueing proves only that ADO accepted a run: verify pipeline
460 completion and its image tag separately.

The latest main build inspected during this review failed resolving Gradle plugin
dependencies before reaching publication. That is a separate build failure, not
proof of a new publishing 401. Recheck it in CI; no builds or deployments were
queued as part of this local change.
