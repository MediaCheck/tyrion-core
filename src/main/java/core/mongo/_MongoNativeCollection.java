package core.mongo;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static core.mongo._SubscriberHelpers.ObservableSubscriber;
import static core.mongo._SubscriberHelpers.OperationSubscriber;


@SuppressWarnings({"rawtypes", "unchecked"})
public class _MongoNativeCollection {

    private static final Logger logger = LoggerFactory.getLogger(_MongoNativeCollection.class);


    private MongoDatabase database;
    private  MongoCollection<Document> collection;

    public _MongoNativeCollection(MongoDatabase database, String collection_name) {

        this.database = database;
        collection = database.getCollection(collection_name);
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }


    /**
     * Drop all the data in it
     * @throws Throwable
     */
    public ObservableSubscriber<Success> dropCollection() throws Throwable {

        logger.trace("dropCollection:: collection name: {}", this.collection.getNamespace());
        // Create subscriberr
        ObservableSubscriber subscriber = new ObservableSubscriber<Success>();

        logger.trace("dropCollection:: subscriber created");
        // Call Action
        this.collection.drop().subscribe(subscriber);

        logger.trace("dropCollection:: subscribed");

        // Provide Action
        return  subscriber;

    }


    /**
     * @param doc
     */
    public ObservableSubscriber<Success> insertDocumentToCollection(Document doc) {

        logger.trace("insertDocumentToCollection:: collection name: {}",  this.collection.getNamespace());
        // Create subscriber
        OperationSubscriber subscriber = new OperationSubscriber<Success>();

        logger.trace("insertDocumentToCollection:: subscriber created");
        // Call Action
        collection.insertOne(doc).subscribe(subscriber);

        logger.trace("insertDocumentToCollection:: subscribed");

        // Provide Action
        return subscriber;

    }

    /**
     *
     * @param docs
     * @throws Throwable
     */
    public ObservableSubscriber<Success> insertDocumentToCollection(List<Document> docs) throws Throwable {

        logger.trace("insertDocumentToCollection:: collection name: {}, documents: {}",  this.collection.getNamespace(), docs.size());
        // Create subscriber
        ObservableSubscriber subscriber = new ObservableSubscriber<Success>();

        logger.trace("insertDocumentToCollection::  subscriber created");
        // Call Action
        this.collection.insertMany(docs).subscribe(subscriber);

        logger.trace("insertDocumentToCollection::  subscribed");

        // Provide Action
        return subscriber;

    }


    public ObservableSubscriber<Document> getDocumentNonBlocking( Bson filter) {

        // Create subscriber
        ObservableSubscriber subscriber = new ObservableSubscriber<Document>();

        // Call Action
        this.collection.find(filter).subscribe(subscriber);

        // Provide Action
        return subscriber;
    }


    public ObservableSubscriber<DeleteResult> deleteDocument( Bson filter) {

        logger.trace("deleteDocument:: collection name: {}, filter: {}", this.collection.getNamespace(), filter.toString());
        // Create subscriber
        OperationSubscriber subscriber = new OperationSubscriber<DeleteResult>();

        logger.trace("deleteDocument:: subscriber created");
        // Call Action
        this.collection.deleteMany(filter).subscribe(subscriber);

        // Provide Action
        return subscriber;

    }

    public MongoDatabase getMongoDatabase() {
        return this.database;
    }


}
