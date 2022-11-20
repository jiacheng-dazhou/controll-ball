package com.csdtb.common.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-18
 **/
@Getter
public enum CalculationQuestionsEnum {

    EASY_LEVEL(1,"简单难度题库(一位数加减法)","2+2","2+3","1+4","1+3","1+4","1+6","2+7","3+4","4+5","6+2","9-2","7-3","4-2","6-3","8-3","5-1","7-6","8-7","9-9","3-2","1+8","2+4","2+5","2+6","2+7","3+1","3+3","3+2","3+6","3+5","9-1","9-3","9-5","9-4","9-7","9-6","9-8","8-1","8-2","8-5","8-4","8-7","8-6","8-8","4+1","4+3","4+2","4+4","4+5","5+4"),
    GENERAL_LEVEL(2,"一般难度题库(两位数加减法，不进位不退位，答案是两位数)","22+11","33+21","32+44","47+12","17+22","35+63","45+14","26+71","71+14","35+44","99-33","76-14","38-12","46-25","88-34","74-24","67-11","48-17","57-37","69-57"),
    DIFFICULTY_LEVEL(3,"困难难度题库(两位数加减法（进位退位，答案是两位数)","72+19","27+14","35+27","77+19","16+27","53+17","72+19","49+16","55+26","66+15","72-18","66-19","55-37","44-18","98-79","53-17","81-47","34-16","73-19","93-68");

    private Integer level;
    private String description;
    private String[] questions;

    CalculationQuestionsEnum(Integer level, String description, String... questions) {
        this.level = level;
        this.description = description;
        this.questions = questions;
    }

    /**
     * 根据难度获取相应题量
     * @param level
     * @param limit
     * @return
     */
    public static List<String> getQuestionsByLevel(int level,int limit){
        return Arrays.stream(Arrays.stream(CalculationQuestionsEnum.class.getEnumConstants())
                .filter(item -> item.getLevel().equals(level))
                .map(CalculationQuestionsEnum::getQuestions)
                .collect(Collectors.toList())
                .get(0))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
