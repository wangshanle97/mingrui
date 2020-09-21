package com.baidu.shop.utils;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.elasticsearch.core.SearchHit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName ESHighLightUtil
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/16
 * @Version V1.0
 **/
public class ESHighLightUtil<T> {
    public static HighlightBuilder getHighBrightBuilder(String ...highLightField){
        HighlightBuilder highlightBuilder = new HighlightBuilder();

        Arrays.asList(highLightField).forEach(hlf -> {
            HighlightBuilder.Field field = new HighlightBuilder.Field(hlf);
            field.preTags("<span style='color:red'>");
            field.postTags("</span>");

            highlightBuilder.field(field);
        });
        return highlightBuilder;
    }

    public static <T> List<SearchHit<T>> getHighLightHit(List<SearchHit<T>> list) {

        return list.stream().map(tSearchHit -> {
            Map<String, List<String>> highlightFields = tSearchHit.getHighlightFields();
            highlightFields.forEach((key, value) -> {
                T content = tSearchHit.getContent();
                try {
                    Method method = content.getClass().getMethod("set" +
                            String.valueOf(key.charAt(0)).toUpperCase() + key.substring(1), String.class);
                    method.invoke(content, value.get(0));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
            return tSearchHit;
        }).collect(Collectors.toList());
    }
}
