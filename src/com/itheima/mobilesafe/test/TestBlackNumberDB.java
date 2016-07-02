package com.itheima.mobilesafe.test;

import java.util.List;
import java.util.Random;

import com.itheima.mobilesafe.db.BlackNumberDBOpenHelper;
import com.itheima.mobilesafe.db.dao.BlackNumberDao;
import com.itheima.mobilesafe.domain.BlackNumberInfo;

import android.test.AndroidTestCase;

public class TestBlackNumberDB extends AndroidTestCase {

    public void testCreateDB() throws Exception {
        BlackNumberDBOpenHelper helper = new BlackNumberDBOpenHelper(getContext());
        helper.getWritableDatabase();
    }

    public void testAdd() throws Exception {
        BlackNumberDao dao = new BlackNumberDao(getContext());
        long basenumber = 13500000001l;
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            dao.add(String.valueOf(basenumber + i), String.valueOf(random.nextInt(3) + 1));
        }
    }

    public void testFindAll() throws Exception {
        BlackNumberDao dao = new BlackNumberDao(getContext());
        List<BlackNumberInfo> infos = dao.findAll();
        for (BlackNumberInfo info : infos) {
            System.out.println(info.toString());
        }
    }

    public void testDelete() throws Exception {
        BlackNumberDao dao = new BlackNumberDao(getContext());
        dao.delete("13500000100");
    }

    public void testUpdate() throws Exception {
        BlackNumberDao dao = new BlackNumberDao(getContext());
        dao.update("13500000100", "1");
    }

    public void testFind() throws Exception {
        BlackNumberDao dao = new BlackNumberDao(getContext());
        boolean result = dao.find("13500000100");
        assertEquals(true, result);
    }
}
