package com.pit.excel.write;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

/**
 * ExcelWriterOperator 写excel sheet页操作类
 *
 * @author gy
 * @date 2020/3/17
 */
public abstract class ExcelWriterOperator<T> {
	private List<T> beans;
	private int columnNum;

	/**
	 * @param beans
	 * @param columnNum 当前sheet页的列数
	 */
	public ExcelWriterOperator(List<T> beans, int columnNum) {
		this.beans = beans;
		this.columnNum = columnNum;
	}

	/**
	 * 填充sheet页
	 *
	 * @param workbook
	 */
	protected void fillSheet(XSSFWorkbook workbook) {
		String sheetName = innerGetSheetName();

		XSSFSheet sheet = workbook.createSheet(sheetName);
		createHead(sheet);
		fillContent(sheet);
	}

	/**
	 * 创建表头
	 *
	 * @param sheet
	 */
	private void createHead(XSSFSheet sheet) {
		List<ExcelHeadDes> excelHeadDesList = innerGetExcelHead();

		XSSFRow headRow = sheet.createRow(0);
		for (int i = 0; i < excelHeadDesList.size(); i++) {
			ExcelHeadDes excelHeadDes = excelHeadDesList.get(i);
			String name = excelHeadDes.getName();
			int width = excelHeadDes.getWidth();
			headRow.createCell(i).setCellValue(name);

			if (width > 0) {
				sheet.setColumnWidth(i, width);
			}
		}
	}

	/**
	 * 填充sheet页内容
	 *
	 * @param sheet
	 */
	private void fillContent(XSSFSheet sheet) {
		int numOfRows = 1;
		for (T bean : beans) {
			if (!check(bean)) {
				continue;
			}

			List<String> cellValues = innerGetCellValues(bean);

			XSSFRow row = sheet.createRow(numOfRows);
			for (int i = 0; i < cellValues.size(); i++) {
				row.createCell(i).setCellValue(cellValues.get(i));
			}

			numOfRows++;
		}
	}

	/**
	 * 检查<T>是否合法
	 *
	 * @param bean
	 * @return
	 */
	protected boolean check(T bean) {
		return true;
	}

	private List<String> innerGetCellValues(T bean) {
		List<String> cellValues = getCellValues(bean);
		Validate.isTrue(CollectionUtils.isNotEmpty(cellValues), "cell values must be not empty");
		Validate.isTrue(cellValues.size() == columnNum, "cell values number must be " + columnNum);

		return cellValues;
	}

	protected abstract List<String> getCellValues(T bean);

	/**
	 * 得到excel表头，并检查
	 *
	 * @return
	 */
	private List<ExcelHeadDes> innerGetExcelHead() {
		List<ExcelHeadDes> excelHeadDes = getExcelHeadDes();
		Validate.isTrue(CollectionUtils.isNotEmpty(excelHeadDes), "head names must be not empty");
		Validate.isTrue(excelHeadDes.size() == columnNum, "head number must be " + columnNum);

		return excelHeadDes;
	}

	/**
	 * excel表头
	 *
	 * @return
	 */
	protected abstract List<ExcelHeadDes> getExcelHeadDes();

	private String innerGetSheetName() {
		String sheetName = getSheetName();
		Validate.isTrue(StringUtils.isNotBlank(sheetName), "sheetName must be not empty");

		return sheetName;
	}

	/**
	 * sheet页名
	 *
	 * @return
	 */
	protected abstract String getSheetName();

	protected void finish() {
	}

	@Data
	@Builder
	public static class ExcelHeadDes {
		private String name;
		private int width;
	}
}
