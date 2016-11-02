package pl.droidcon.app.helper;


import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public interface Mapper<ApiObject, DatabaseObject extends RealmObject> {

    DatabaseObject map(ApiObject apiObject);

    List<ApiObject> fromDBList(List<DatabaseObject> databaseObjects);

    ApiObject fromDB(DatabaseObject databaseObject);
}
