package com.dominik.jobapplicationservice.cv.service;

import com.dominik.jobapplicationservice.cv.entity.CV;
import com.dominik.jobapplicationservice.jobs.entity.JobOffer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CVTailoringService {
    
    private final DeepSeekService deepSeekService;
    private final CVExtractionService cvExtractionService;
    private final CVTextPatternService cvTextPatternService;
    
    public CVTailoringService(DeepSeekService deepSeekService, 
                              CVExtractionService cvExtractionService,
                              CVTextPatternService cvTextPatternService) {
        this.deepSeekService = deepSeekService;
        this.cvExtractionService = cvExtractionService;
        this.cvTextPatternService = cvTextPatternService;
    }
    
    @Getter
    public static class TailoredContent {
        private String summary;
        private String experience;
        private Map<String, String> keySkills;
        
        public TailoredContent(String summary, String experience, Map<String, String> keySkills) {
            this.summary = summary;
            this.experience = experience;
            this.keySkills = keySkills;
        }
    }
    
    public TailoredContent tailorCVForJob(CV originalCV, JobOffer jobOffer) {
        try {
            String fullText = cvExtractionService.extractTextFromPDF(originalCV.getFilePath());
            Map<String, CVTextPatternService.CVSectionContent> sections = cvTextPatternService.extractCVSectionsByPattern(fullText);
            
            String tailoredSummary = tailorSummary(sections.get("SUMMARY").getContent(), jobOffer);
            String tailoredExperience = tailorExperience(sections.get("EXPERIENCE").getContent(), jobOffer);
            Map<String, String> tailoredSkills = tailorSkills(sections.get("KEY_SKILLS").getContent(), jobOffer);
            
            return new TailoredContent(tailoredSummary, tailoredExperience, tailoredSkills);
            
        } catch (Exception e) {
            log.error("Error tailoring CV for job: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to tailor CV", e);
        }
    }
    
    private String tailorSummary(String originalSummary, JobOffer jobOffer) {
        String prompt = String.format("""
            Rewrite this summary to match the target job requirements using relevant keywords.
            
            Original Summary:
            %s
            
            Target Job: %s at %s
            Job Requirements: %s
            
            Rules:
            1. Keep the same experience level (do NOT add "Senior", "Lead", etc.)
            2. Use keywords from job requirements that match your actual skills
            3. Emphasize technologies mentioned in job posting if you have them
            4. Maintain authentic experience level and tone
            5. Replace generic terms with specific job-relevant ones
            6. Keep same length and professional structure
            
            Return only the rewritten summary text, nothing else.
            """, originalSummary, jobOffer.getTitle(), jobOffer.getCompany(), jobOffer.getDescription());
        
        return callAI(prompt);
    }
    
    private String tailorExperience(String originalExperience, JobOffer jobOffer) {
        String prompt = String.format("""
            Rewrite this work experience to match job requirements using relevant keywords.
            
            Original Experience:
            %s
            
            Target Job: %s at %s
            Job Requirements: %s
            
            Rules:
            1. Keep ALL job titles, companies, and dates exactly as written
            2. Reorder bullet points to put most relevant ones first
            3. Rewrite bullet points using keywords from job requirements
            4. Focus on technologies and skills mentioned in job posting
            5. Keep achievements factual but emphasize job-relevant aspects
            6. Use job posting terminology where appropriate
            7. Maintain original structure and formatting
            
            Return only the tailored experience section, nothing else.
            IMPORTANT: Return ONLY the tailored experience section text that will replace the original CV content.
            Do NOT include any conversational responses, explanations, suggestions, or additional commentary.
            Do NOT ask if I want further tailoring or mention anything about customization.
            Preserve the original structure and formatting exactly.
            """, originalExperience, jobOffer.getTitle(), jobOffer.getCompany(), jobOffer.getDescription());
        
        return callAI(prompt);
    }
    
    private Map<String, String> tailorSkills(String originalSkills, JobOffer jobOffer) {
        String prompt = String.format("""
            Reorder and emphasize skills relevant to the target job.
            
            Original Skills:
            %s
            
            Target Job: %s at %s
            Job Requirements: %s
            
            Rules:
            1. Prioritize skills mentioned in job requirements first
            2. Keep same category structure (Frontend, Backend, etc.)
            3. Can add related skills if they match job requirements
            4. Reorder within categories to emphasize job-relevant technologies
            5. Use terminology from job posting where applicable
            
            Return ONLY this JSON format, nothing else:
            {
                "Frontend": "skill1, skill2, skill3...",
                "Backend": "skill1, skill2, skill3...",
                "Architecture & Patterns": "skill1, skill2, skill3...",
                "DevOps & Tools": "skill1, skill2, skill3...",
                "Testing & Quality": "skill1, skill2, skill3..."
            }
            Do NOT include any conversational responses, explanations, suggestions, or additional commentary.
            """, originalSkills, jobOffer.getTitle(), jobOffer.getCompany(), jobOffer.getDescription());
        
        String response = callAI(prompt);
        
        Map<String, String> skillsMap = new HashMap<>();
        skillsMap.put("skills_response", response);
        return skillsMap;
    }
    
    private String callAI(String prompt) {
        return deepSeekService.generateText(prompt);
    }
}