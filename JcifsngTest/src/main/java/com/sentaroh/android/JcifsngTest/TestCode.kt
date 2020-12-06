package com.sentaroh.android.JcifsngTest

/*
The MIT License (MIT)
Copyright (c) 2011-2019 Sentaroh

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

*/

import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.util.Properties


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
//        val creds = NtlmPasswordAuthentication(bc, domain_name, account_name, account_password)
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
