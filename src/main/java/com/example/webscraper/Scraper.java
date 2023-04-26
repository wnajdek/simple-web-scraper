package com.example.webscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class Scraper {

    public List<News> getBBCNewsArticles(String baseUrl) {
        List<String> links = getLinksToArticles(baseUrl);

        List<News> news = new ArrayList<>();
        for (String link : links) {
            News bbcArticle = getBBCArticle(link);
            if (bbcArticle != null) {
                news.add(bbcArticle);
            }
        }

        return news;
    }

    public List<String> getLinksToArticles(String baseUrl) {
        baseUrl = baseUrl.replaceFirst("/*$", "");
        try {
            Document document = Jsoup.connect(baseUrl + "/news").get();

            Elements aElements = document.getElementsByTag("a");

            List<String> links = new ArrayList<>();
            for (Element e : aElements) {
                String link = e.attr("href");

                if (!link.contains("/news/")) continue;

                if (!link.contains(baseUrl)) {
                    link = baseUrl + link;
                }

                links.add(link);
            }

            return links;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public News retrieveArticle(String link, String source) {
        if ("wikipedia".equalsIgnoreCase(source)) {
            return getWikipediaArticle(link);
        } else if ("bbc".equalsIgnoreCase(source)) {
            return getBBCArticle(link);
        }

        return new News("NOT_FOUND", "NOT_FOUND", "NOT_FOUND");
    }

    public News getBBCArticle(String link) {
        System.out.println("link: " + link);
        try {
            Document document = Jsoup.connect(link).get();

            String heading = document.getElementById("main-heading").text();

            Elements textBlocks = document.getElementsByAttributeValue("data-component", "text-block");

            StringBuilder sb = new StringBuilder();
            for (Element textBlock : textBlocks) {
                sb.append(textBlock.getElementsByTag("p").get(0).text())
                        .append("\n");
            }

            return new News(heading, sb.toString(), link);

        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    public News getWikipediaArticle(String link) {
        try {
            Document document = Jsoup.connect(link).get();

            String heading = document.getElementById("firstHeading").child(0).text();

            Elements paragraphs = document.getElementsByTag("p");

            StringBuilder sb = new StringBuilder();
            for (Element e : paragraphs) {
                sb.append(e.text()).append("\n");
            }

            return new News(heading, sb.toString(), link);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
