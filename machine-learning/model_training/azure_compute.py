from azureml.core import Workspace
from azureml.core.compute import ComputeInstance
from azureml.core.compute_target import ComputeTargetException
from azureml.core.authentication import ServicePrincipalAuthentication

if __name__=='__main__':
    sp = ServicePrincipalAuthentication(tenant_id="f70874a7-6e94-4cc9-9a3a-61dbe23c9955",  # tenantID
                                        service_principal_id="fa700399-a952-4f65-85fe-d2b3ff88007e",  # clientId
                                        service_principal_password="kZipvNWoqj7TLVocijFDFdS~URZ1gOEU70")  # clientSecret

    ws = Workspace.get(name="recommender",
                       auth=sp,
                       subscription_id="f4126c71-f302-4cde-9386-b42a907f292d",
                       resource_group="favor8")

    # Choose a name for your instance
    # Compute instance name should be unique across the azure region
    compute_name = "recommender-train"

    # Verify that instance does not exist already
    try:
        instance = ComputeInstance(workspace=ws, name=compute_name)
        print('Found existing instance, use it.')
    except ComputeTargetException:
        compute_config = ComputeInstance.provisioning_configuration(
            vm_size='STANDARD_F2S_V2',
            ssh_public_access=False
        )
        instance = ComputeInstance.create(ws, compute_name, compute_config)
        instance.wait_for_completion(show_output=True)