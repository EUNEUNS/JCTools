package net.lifove.research.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	static public String getNextDate(String ddmmyyyyDate,String soruceFormat,String targetFormat) {
		String strNextDate = "";
		try {
			Date date = new SimpleDateFormat(soruceFormat).parse(ddmmyyyyDate); //"d/M/yyyy"
			Date nextDate = new Date(date.getTime() + (1000 * 60 * 60 * 24));
			SimpleDateFormat ft = new SimpleDateFormat (targetFormat); // d/M/yy
			strNextDate = ft.format(nextDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strNextDate;
	}

}
