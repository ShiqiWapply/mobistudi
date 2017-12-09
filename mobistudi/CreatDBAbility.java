package cat.mobistudi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Shiqi wang
 * @date 2017/8/20
 */

public class CreateDBAbility extends SQLiteOpenHelper {

    public static final String CREATE_ABILITY="create table Ability ("
            +"id integer primary key autoincrement, "
            +"discrimination text, "
            +"difficult text, "
            +"result integer)";

    private Context mContext;

    public CreateDBAbility(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_ABILITY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists Ability");
        onCreate(sqLiteDatabase);
    }
}