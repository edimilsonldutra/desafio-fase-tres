# Terraform Backend Configuration
# Store state remotely in S3 with DynamoDB locking

terraform {
  backend "s3" {
    # S3 bucket for state storage (must be created beforehand)
    bucket = "fiap-terraform-state-${var.aws_region}"
    key    = "${var.project_name}/${var.environment}/terraform.tfstate"
    region = var.aws_region

    # Encryption
    encrypt        = true
    kms_key_id     = "alias/terraform-state"

    # State locking with DynamoDB
    dynamodb_table = "terraform-state-lock"

    # Workspace isolation
    workspace_key_prefix = "workspaces"

    # Versioning (recommended for S3 bucket)
    # Enable versioning on the S3 bucket to keep state history
  }
}

# NOTE: Before using this backend, you must:
# 1. Create the S3 bucket: aws s3 mb s3://fiap-terraform-state-us-east-1
# 2. Enable versioning: aws s3api put-bucket-versioning --bucket fiap-terraform-state-us-east-1 --versioning-configuration Status=Enabled
# 3. Enable encryption: aws s3api put-bucket-encryption --bucket fiap-terraform-state-us-east-1 --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"aws:kms","KMSMasterKeyID":"alias/terraform-state"}}]}'
# 4. Create DynamoDB table: aws dynamodb create-table --table-name terraform-state-lock --attribute-definitions AttributeName=LockID,AttributeType=S --key-schema AttributeName=LockID,KeyType=HASH --billing-mode PAY_PER_REQUEST
# 5. Block public access on S3 bucket

