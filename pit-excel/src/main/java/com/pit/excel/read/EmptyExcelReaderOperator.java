package com.pit.excel.read;


import org.apache.poi.ss.usermodel.Row;

/**
 * EmptyExcelReaderOperator
 *
 * @author gy
 * @date 2020/3/23
 */
public class EmptyExcelReaderOperator extends ExcelReaderOperator {
    @Override
    protected Object convertRow(Row row) {
        return "";
    }

    @Override
    protected int headRowNum() {
        return 0;
    }
}
