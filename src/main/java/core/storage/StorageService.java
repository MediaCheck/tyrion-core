package core.storage;

import com.google.inject.ImplementedBy;

import java.io.File;
import java.util.concurrent.CompletionStage;

@ImplementedBy(AmazonStorageService.class)
public interface StorageService {

    CompletionStage<? extends StorageItem> store(File file, String content_type, String name, String path);

    CompletionStage<? extends StorageItem> store(String file, String content_type, String name, String path);

    CompletionStage<? extends StorageItem> store(byte[] file, String content_type, String name, String path);

    CompletionStage<? extends StorageItem> duplicate(StorageItem item, String path);

    CompletionStage<RetrievedItem> retrieve(StorageItem item);

    CompletionStage<Void> remove(StorageItem item);
}
