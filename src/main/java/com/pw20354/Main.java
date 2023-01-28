package com.pw20354;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.ScreenshotAnimations;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import static java.awt.Color.magenta;

public class Main {

    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Page page = setupPage(playwright);
            Locator myElement = page.locator("ul.et_pb_social_media_follow");
            myElement.scrollIntoViewIfNeeded();
            byte[] elementScreenshot = myElement.screenshot();
            byte[] elementScreenshotWOAnimations = myElement.screenshot(new Locator.ScreenshotOptions().setAnimations(ScreenshotAnimations.DISABLED));

            BufferedImage diffBI = imageDiff(elementScreenshot, elementScreenshotWOAnimations);
            byte[] diff = convertBufferedImageToByteArray(diffBI, "png");

            saveToFile(elementScreenshot, "results/element-no-options.png");
            saveToFile(elementScreenshotWOAnimations, "results/element-options-animations-disabled.png");
            saveToFile(diff, "results/element-diff.png");
        }
    }

    private static void saveToFile(byte[] elementScreenshot, String targetFileName) {
        File outputFile = new File(targetFileName);
        try {
            FileUtils.writeByteArrayToFile(outputFile, elementScreenshot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage imageDiff(byte[] elementScreenshot, byte[] elementScreenshotWOAnimations) {
        BufferedImage elementScreenshotBI = convertByteArrayToBufferedImage(elementScreenshot);
        BufferedImage elementScreenshotWOAnimationsBI = convertByteArrayToBufferedImage(elementScreenshotWOAnimations);
        ImageComparisonResult imageComparisonResult = new ImageComparison(elementScreenshotBI, elementScreenshotWOAnimationsBI).setThreshold(0)
                .setDifferenceRectangleFilling(true, 5)
                .setExcludedRectangleFilling(true, 5)
                .setRectangleLineWidth(4).setDifferenceRectangleColor(magenta)   // use java.awt.Color or new Color(int r, int  g, int  b) to get contrasting box color constants
                .compareImages();
        System.out.println("Percentage difference is " + imageComparisonResult.getDifferencePercent());
        return imageComparisonResult.getResult();
    }

    /**
     * open browser, setup page, open page
     * @param playwright Playwright object
     * @return page
     */
    private static Page setupPage(Playwright playwright) {
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false));
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        page.navigate("https://qualityminds.com/de/portfolio/");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Alle akzeptieren")).click();
        return page;
    }

    /**
     * converts image stored as byte array to BufferedImage variable
     *
     * @param inputByteArray image stored as byte[] variable
     * @return image stored as BufferedImage type
     */
    public static BufferedImage convertByteArrayToBufferedImage(byte[] inputByteArray) {
        try {
            InputStream isScreenshot = new ByteArrayInputStream(inputByteArray);
            return ImageIO.read(isScreenshot);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converts image stored as BufferedImage to byte array.
     *
     * @param imageBI image stored as BufferedImage
     * @param fileFormat format for byte[] image, such as "png"
     * @return byte[] image
     */
    public static byte[] convertBufferedImageToByteArray(BufferedImage imageBI, String fileFormat) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(imageBI, fileFormat, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            System.out.println("Conversion from buffered image to byte array IOException");
        }
        System.out.println("Conversion from buffered image to byte array failed.");
        return null;
    }


}