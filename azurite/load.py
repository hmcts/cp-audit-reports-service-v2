from os import getenv
from azure.data.tables import TableServiceClient
from azure.core.credentials import AzureNamedKeyCredential

service = TableServiceClient(
    endpoint = getenv('AZURE_ENDPOINT'),
    credential = AzureNamedKeyCredential(getenv('AZURE_CREDS_NAME'), getenv('AZURE_CREDS_KEY'))
)
reportrequests = service.get_table_client('reportrequests')

reportrequests.upsert_entity({
    "PartitionKey": "1",
    "RowKey": "1234",
    "auditUserEmail": "audit@example.com",
    "auditReportReference": "2025-03-02-XxXXxX",
    "searchCriteria": "ALL_ACTIVITY",
    "pipelineJobId": "550e8400-e29b-41d4-a716-446655440000",
    "pipelineStatus": "PENDING"
})


