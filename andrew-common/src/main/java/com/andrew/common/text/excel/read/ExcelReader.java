package com.andrew.common.text.excel.read;

import com.andrew.common.exception.CommonException;
import com.andrew.common.exception.ResultCodes;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ExcelReader 读excel操作类
 *
 * @author Andrew
 * @date 2020/3/20
 */
public class ExcelReader {
	public static void read(InputStream inputStream, String fileName, List<ExcelReaderOperator> operators) throws Exception {
		Workbook workbook = null;

		try {
			workbook = getReadWorkBookType(inputStream, fileName);
			for (int i = 0; i < operators.size(); i++) {
				ExcelReaderOperator operator = operators.get(i);

				// 跳过EmptyExcelReaderOperator
				if (operator instanceof EmptyExcelReaderOperator) {
					continue;
				}

				// 获取第一个sheet
				Sheet sheet = workbook.getSheetAt(i);
				// 忽略表头，从开始startRowNum读取
				int rowCount = sheet.getLastRowNum();

				int headNum = operator.headRowNum();
				boolean needCheckHead = operator.needCheckHead();

				if (needCheckHead) {
					List<Row> headRows = new ArrayList<>();
					for (int rowNum = 0; rowNum < headNum; rowNum++) {
						Row row = sheet.getRow(rowNum);
						headRows.add(row);
					}
					operator.checkHead(headRows);
				}

				for (int rowNum = headNum; rowNum <= rowCount; rowNum++) {
					Row row = sheet.getRow(rowNum);
					operator.addRowValue(row);
				}
				operator.finish();
			}
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(workbook);
		}
	}

	private static Workbook getReadWorkBookType(InputStream inputStream, String fileName) throws IOException {
		//xls-2003, xlsx-2007
		try {
			String toLower = fileName.toLowerCase();
			if (toLower.endsWith("xlsx")) {
				return new XSSFWorkbook(inputStream);
			} else if (toLower.endsWith("xls")) {
				return new HSSFWorkbook(inputStream);
			} else {
				//  抛出自定义的业务异常
				throw new CommonException(ResultCodes.CommonResultCode.NO_SUPPORT_FILE_TYPE.getRealResultCode("excel", "xlsx、xls", toLower));
			}
		} catch (IOException e) {
			//  抛出自定义的业务异常
			throw e;
		}
	}

	public static void read(String sourceFilePath, List<ExcelReaderOperator> operators) throws Exception {
		read(new FileInputStream(sourceFilePath), sourceFilePath, operators);
	}
}
