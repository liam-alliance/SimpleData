SimpleData
==========

Android ORM Library

Lightweight orm layer for storing data in sqlite databases.


Example 1:

A Settings Table For Your Android App

Step 1:

Create a class that extends SimpleDataItem:

@DatabaseTable (tableName = "MyAppSettings", primaryKeyFieldName = "settingsId")
public class SettingsDataItem extends SimpleDataItem {
        @DatabaseField (type = SimpleDataField.FIELD_TYPE_INTEGER, primaryKey = true, autoNumber = true)
        public int settingsId;

        @DatabaseField (type = SimpleDataField.FIELD_TYPE_TEXT, maxLength = 64)
        public String mySettingsField1;

        @DatabaseField (type = SimpleDataField.FIELD_TYPE_TEXT, maxLength = 16, required = true, default = "hello")
        public String mySettingsField2;

        @DatabaseField (type = SimpleDataField.FIELD_TYPE_INTEGER, min = 0, max = 65535, required = true)
        public int mySettingsField3;

        @DatabaseField (type = SimpleDataField.FIELD_TYPE_NUMERIC, min = 0, max = 65535, required = true)
        public double mySettingsField4;

}

Note that the annotations @DatabaseTable and @DatabaseField are required, and allow you to specify constraints on your field values

Field Types And Options
- SimpleDataField.FIELD_TYPE_INTEGER
  - primaryKey (bool) 
  - autoNumber (bool)
  - max (int) 
  - min (int)
  - required (bool)
  - default (int)

- SimpleDataField.FIELD_TYPE_TEXT
  - required (bool)
  - maxLength (int)
  - minLength (int)
  - default (String)

- SimpleDataField.FIELD_TYPE_NUMERIC
  - required (bool)
  - max (double)
  - min (double)
  - default (double)
  - precision (int)



Step 2 (Optional):

Create a class that extends SimpleDataSet:
(In this example, we only ever want 1 row in the table, so we use this class to ensure that we dont 
get multiple settings rows)

public class SettingsDataSet extends SimpleDataSet {
        private static SettingsDataItem currentSettings;

        public SettingsDataSet(Context applicationContext, boolean keepConnectionOpen) {
                super(applicationContext, SettingsDataItem.class, keepConnectionOpen);
        }

        public SettingsDataSet(Context applicationContext) {
                super(applicationContext, SettingsDataItem.class);
        }

        public SettingsDataItem getSettings() {
                if (currentSettings == null) {
                        // get the only record in the table
                        currentSettings = (SettingsDataItem) selectFirst();

                        // if it doesn't exist, this is probably the first time being run.
                        if (currentSettings == null) {
                                SettingsDataItem item = new SettingsDataItem();
                                item.mySettingsField1 = "A sample value";
                                item.mySettingsField2 = "A sample value";
                                item.mySettingsField3 = 123;
                                item.mySettingsField4 = 123.456;

                                insert(item);
                                currentSettings = (SettingsDataItem) selectFirst();
                                // if it is still null, we cant write to the db, throw an error
                                if (currentSettings == null) {
                                        return null;
                                }

                        }

                }
                return currentSettings;
        }
}

Using these settings in your application:
If you've made the optional SettingsDataSet class, you can:

Get Your App Settings:
  SettingsDataSet dataSet = new SettingsDataSet(<application context>);
  SettingsDataItem settings = dataSet.getSettings();

  now you can access your settings, eg:
  Log.d("Settings - Value 1", settings.mySettingsField1);

Updating and Saving Settings:
  settings.mySettingsField1 = "Hello World";
  dataSet.update(settings);


If you haven't made the option SettingsDataSet class, you can:

Get Your App Settings:
  SimpleDataSet dataSet = new SimpleDataSet(<application context>, SettingsDataItem.class);
  SettingsDataItem settings = dataSet.selectFirst();
  if (settings == null) {
      settings = new SettingsDataItem();
      settings.mySettingsField1 = "Hello World";
      ..
      ..
      dataSet.insert(settings);
  }



