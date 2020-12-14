package core.exceptions;

import java.util.UUID;

/**
 * Exception which indicates that some entity was not found in the database.
 */
public class NotFoundException extends BaseException {

    private Class entity;
    private UUID uuid_id;

    public NotFoundException(Class entity) {
        super("Could not find " + entity.getSimpleName());
        this.entity = entity;
    }

    public NotFoundException(Class entity, String message) {
        super("Could not find " + entity.getSimpleName() + " Message: " + message);
        this.entity = entity;
    }


    public NotFoundException(Class entity, UUID uuid_id) {
        super("Could not find " + entity.getSimpleName() + " by ID: " + uuid_id );
        this.entity = entity;
        this.uuid_id = uuid_id;
    }


    public NotFoundException(Class entity, String message, UUID uuid_id) {
        super("Could not find " + entity.getSimpleName() + " by ID: " + uuid_id +  " Message: " + message);
        this.entity = entity;
        this.uuid_id = uuid_id;
    }

    public Class getEntity() {
        return entity;
    }
}