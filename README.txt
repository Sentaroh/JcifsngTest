JcifsngTest

This software includes the work that is distributed in the Apache License 2.0.
This software includes the work that is distributed in the GNU Lesser General Public License v2.1.

jcifs-ngをAndroidでテストするためのアプリです。
ログメッセージは画面とファイル(/storage/emulated/0/Android/data/com.sentaroh.android.JcifsNgTest/files/JcifsNgTest_log.txt)に出力します。（ファイルは毎回上書きされます）

使い方：
\JcifsngTest\JcifsngTest\src\main\java\com\sentaroh\android\JcifsngTest\TestCode.javaを編集してAndroidStudioでBuild/Runするだけです。

public class TestCode {

    static public void runTest(MainActivity activity, Properties prop, String domain_name, String account_name, String account_password)
            throws MalformedURLException, CIFSException {
//      書き出すログの設定です必要に応じて変更して下さい、falseは出力しません
        boolean debug=true;
        boolean trace=true;
        boolean info=true;
        boolean warning=true;
        boolean error=true;
        mLog.setLogOption(debug, error, info, trace, warning);

//ここからテストコード
        prop.setProperty("jcifs.smb.client.minVersion", "SMB202");
        prop.setProperty("jcifs.smb.client.maxVersion", "SMB300");

        BaseContext bc = new BaseContext(new PropertyConfiguration(prop));
        NtlmPasswordAuthentication creds = new NtlmPasswordAuthentication(bc, domain_name, account_name, account_password);
        CIFSContext ct = bc.withCredentials(creds);
        SmbFile sf=new SmbFile("smb://192.168.200.128/D/", ct);
        String[] fl=sf.list();
        sf.close();
        for(String item:fl) activity.putLogMsg(item);
//ここまで
    }
