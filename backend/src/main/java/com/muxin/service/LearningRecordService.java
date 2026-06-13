package com.muxin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muxin.model.LearningRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LearningRecordService {

    private static final Logger logger = LoggerFactory.getLogger(LearningRecordService.class);
    private static final String RECORDS_FILE = "learning_records.json";

    @Autowired
    private JsonDataService jsonDataService;

    public List<LearningRecord> getAllRecords() {
        return jsonDataService.readJsonFile(RECORDS_FILE, new TypeReference<List<LearningRecord>>() {});
    }

    public List<LearningRecord> getRecordsByUserId(String userId) {
        return getAllRecords().stream()
                .filter(r -> userId.equals(r.getUserId()))
                .sorted(Comparator.comparing(LearningRecord::getWatchedAt).reversed())
                .collect(Collectors.toList());
    }

    public LearningRecord addRecord(LearningRecord record) {
        if (record.getId() == null) {
            record.setId(UUID.randomUUID().toString().substring(0, 8));
        }
        List<LearningRecord> allRecords = getAllRecords();
        allRecords.add(0, record);
        
        // 可选：限制单个用户的记录数，或者全局记录数
        // 这里暂时不限制，或者只保留最近的 1000 条
        if (allRecords.size() > 2000) {
            allRecords = allRecords.subList(0, 2000);
        }
        
        jsonDataService.writeJsonFile(RECORDS_FILE, allRecords);
        return record;
    }

    public void saveBatch(List<LearningRecord> records) {
        List<LearningRecord> allRecords = getAllRecords();
        allRecords.addAll(records);
        jsonDataService.writeJsonFile(RECORDS_FILE, allRecords);
    }
}
