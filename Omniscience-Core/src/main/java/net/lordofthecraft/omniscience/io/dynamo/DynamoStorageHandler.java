package net.lordofthecraft.omniscience.io.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import net.lordofthecraft.omniscience.Omniscience;
import net.lordofthecraft.omniscience.io.RecordHandler;
import net.lordofthecraft.omniscience.io.StorageHandler;

public class DynamoStorageHandler implements StorageHandler {

    private AmazonDynamoDB dynamoDB;
    private DynamoRecordHandler recordHandler;

    @Override
    public boolean connect(Omniscience omniscience) {
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion("@TODO")
                .build();

        recordHandler = new DynamoRecordHandler(this);
        return false;
    }

    @Override
    public RecordHandler records() {
        return recordHandler;
    }

    @Override
    public void close() {

    }

    AmazonDynamoDB getDynamoDB() {
        return dynamoDB;
    }
}
