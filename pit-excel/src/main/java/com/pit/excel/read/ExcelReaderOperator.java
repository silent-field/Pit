package com.pit.excel.read;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;

/**
 * ExcelReaderOperator 读excel sheet页操作类
 *
 * @author gy
 * @date 2020/3/20
 */
public abstract class ExcelReaderOperator<T> {
	protected List<T> rowValueList = new ArrayList();

	/**
	 * 读取excel row
	 *
	 * @param row
	 */
	public void addRowValue(Row row) {
		T value = convertRow(row);
		if (value == null) {
			return;
		}

		rowValueList.add(value);
		afterAddRowValue(value);
	}

	/**
	 * 转化sheet页中一行为T对象实例
	 *
	 * @param row
	 * @return
	 */
	protected abstract T convertRow(Row row);

	/**
	 * 表头行数
	 *
	 * @return
	 */
	protected abstract int headRowNum();

	/**
	 * 是否需要检查表头
	 *
	 * @return
	 */
	protected boolean needCheckHead() {
		return false;
	}

	/**
	 * 检查表头
	 *
	 * @param headRows
	 * @return
	 */
	protected boolean checkHead(List<Row> headRows) {
		return true;
	}

	/**
	 * 在rowValueList.add()之后触发
	 *
	 * @param value
	 */
	protected void afterAddRowValue(T value) {
	}

	/**
	 * 在当前sheet页读取完之后触发
	 */
	protected void finish() {
	}

	/**
	 * 提取excel cell string value（原始string value）
	 *
	 * @param cell
	 * @return
	 */
	protected String getCellStringVal(Cell cell) {
		if (null == cell) {
			return StringUtils.EMPTY;
		}

		CellType cellType = cell.getCellType();
		switch (cellType) {
			case NUMERIC:
				// 返回字符串
				cell.setCellType(CellType.STRING);
				return cell.getStringCellValue();
			case STRING:
				return cell.getStringCellValue();
			case BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			case FORMULA:
				return cell.getCellFormula();
			case BLANK:
				return "";
			case ERROR:
				return String.valueOf(cell.getErrorCellValue());
			default:
				return StringUtils.EMPTY;
		}
	}

	/**
	 * 提取excel cell string value（小写）
	 *
	 * @param cell
	 * @return
	 */
	protected String getCellStringValAndToLower(Cell cell) {
		return getCellStringVal(cell).toLowerCase();
	}

	/**
	 * 提取excel cell string value（大写）
	 *
	 * @param cell
	 * @return
	 */
	protected String getCellStringValAndToUpper(Cell cell) {
		return getCellStringVal(cell).toUpperCase();
	}
}
