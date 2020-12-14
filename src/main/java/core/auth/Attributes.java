package core.auth;

import core.mongo.model._MongoModel_AbstractPerson;
import play.libs.typedmap.TypedKey;

public class Attributes {
    public static final TypedKey<_MongoModel_AbstractPerson<?>> PERSON = TypedKey.create();
}
