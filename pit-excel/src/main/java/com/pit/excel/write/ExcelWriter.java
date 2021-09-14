package com.pit.excel.write;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * ExcelWriter 写excel操作类
 *
 * @author gy
 * @date 2020/3/17
 */
public class ExcelWriter {
    private static Logger logger = LoggerFactory.getLogger(ExcelWriter.class);

    public static void write(String targetFilePath, List<ExcelWriterOperator> operators) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        try {
            for (int i = 0; i < operators.size(); i++) {
                ExcelWriterOperator operator = operators.get(i);
                operator.fillSheet(workbook);
                operator.finish();
            }
            File xlsxFile = new File(targetFilePath);
            workbook.write(new FileOutputStream(xlsxFile));
        } catch (FileNotFoundException e) {
            logger.error("ExcelWriter write FileNotFoundException", e);
        } catch (IOException e) {
            logger.error("ExcelWriter write IOException", e);
        } finally {
            IOUtils.closeQuietly(workbook);
        }
    }
}
