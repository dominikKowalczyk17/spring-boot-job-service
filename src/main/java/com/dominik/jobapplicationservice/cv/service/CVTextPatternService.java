package com.dominik.jobapplicationservice.cv.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CVTextPatternService {
    
    @Getter
    public static class CVSectionContent {
        private String sectionName;
        private String content;
        
        public CVSectionContent(String sectionName, String content) {
            this.sectionName = sectionName;
            this.content = content.trim();
        }
    }
    
    public Map<String, CVSectionContent> extractCVSectionsByPattern(String fullCVText) {
        Map<String, CVSectionContent> sections = new HashMap<>();

        String summary = extractSummary(fullCVText);
        sections.put("SUMMARY", new CVSectionContent("SUMMARY", summary));

        String experience = extractExperience(fullCVText);
        sections.put("EXPERIENCE", new CVSectionContent("EXPERIENCE", experience));

        Map<String, String> skills = extractKeySkills(fullCVText);
        sections.put("KEY_SKILLS", new CVSectionContent("KEY_SKILLS", formatSkillsAsString(skills)));
        
        return sections;
    }
    
    private String extractSummary(String text) {
        Pattern pattern = Pattern.compile("SUMMARY\\s+(.*?)(?=kowalczyk\\.dominik17@gmail\\.com)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
    
    private String extractExperience(String text) {
        Pattern pattern = Pattern.compile("EXPERIENCE\\s+(.*?)(?=SUMMARY)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String experienceText = matcher.group(1).trim();
            Pattern lastJobPattern = Pattern.compile("Mar 2025 - Present.*?(?=(?:\\n\\s*$|$))", Pattern.DOTALL);
            Matcher lastJobMatcher = lastJobPattern.matcher(text);
            if (lastJobMatcher.find()) {
                experienceText += "\n\n" + lastJobMatcher.group(0);
            }
            return experienceText;
        }
        return "";
    }
    
    private Map<String, String> extractKeySkills(String text) {
        Map<String, String> skillCategories = new HashMap<>();
        
        skillCategories.put("Frontend", extractSkillCategory(text, "Frontend", "EDUCATION"));
        
        skillCategories.put("Backend", extractSkillCategory(text, "Backend", "Architecture & Patterns"));
        
        skillCategories.put("Architecture & Patterns", extractSkillCategory(text, "Architecture & Patterns", "DevOps & Tools"));
        
        skillCategories.put("DevOps & Tools", extractSkillCategory(text, "DevOps & Tools", "Testing & Quality"));
        
        skillCategories.put("Testing & Quality", extractSkillCategory(text, "Testing & Quality", "Łódź"));
        
        return skillCategories;
    }
    
    private String extractSkillCategory(String text, String startMarker, String endMarker) {
        String pattern = startMarker + "\\s+(.*?)(?=" + endMarker + ")";
        Pattern regex = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher matcher = regex.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
    
    private String formatSkillsAsString(Map<String, String> skills) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : skills.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString().trim();
    }
}