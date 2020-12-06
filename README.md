JcifsngTest

This software includes the work that is distributed in the GNU Lesser General Public License v2.1.

jcifs-ngをAndroidでテストするためのアプリです。
ログメッセージは画面とファイル(/storage/emulated/0/Android/data/com.sentaroh.android.JcifsNgTest/files/JcifsNgTest_log.txt)に出力します。（ファイルは毎回上書きされます）

使い方：
\JcifsngTest\JcifsngTest\src\main\java\com\sentaroh\android\JcifsngTest\TestCode.ktを編集してAndroidStudioでBuild/Runするだけです。

```
object TestCode {

    private val mLog = LoggerFactory.getLogger(TestCode::class.java)

    @Throws(IOException::class)
    fun runTest(activity: MainActivity, prop: Properties, domain_name: String, account_name: String, account_password: String) {
        //      書き出すログの設定です必要に応じて変更して下さい、falseは出力しません
        val debug = true
        val trace = true
        val info = true
        val warning = true
        val error = true
        mLog.setLogOption(debug, error, info, trace, warning)

        //ここからテストコード
        prop.setProperty("jcifs.smb.client.minVersion", "SMB202")
        prop.setProperty("jcifs.smb.client.maxVersion", "SMB300")

        val bc = BaseContext(PropertyConfiguration(prop))
        val creds = NtlmPasswordAuthenticator(domain_name, account_name, account_password)
        val ct = bc.withCredentials(creds)
        val sf = SmbFile("smb://192.168.200.128/D/", ct)

        //        InputStream is=sf.getInputStream();
        //
        //        int rc=0;
        //        byte[] buff=new byte[1024];
        //        while((rc=is.read(buff))>0) {
        //
        //        }

        val fl = sf.list()
        sf.close()
        for (item in fl) activity.putLogMsg(item)

    }

}
```


