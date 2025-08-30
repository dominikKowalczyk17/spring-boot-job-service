package com.dominik.jobapplicationservice.cv.service;

import com.dominik.jobapplicationservice.cv.entity.CV;
import com.dominik.jobapplicationservice.jobs.entity.JobOffer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CVTailoringService {
    
    private final AnthropicChatModel anthropicChatModel;
    private final CVExtractionService cvExtractionService;
    private final CVTextPatternService cvTextPatternService;
    
    public CVTailoringService(AnthropicChatModel anthropicChatModel, 
                              CVExtractionService cvExtractionService,
                              CVTextPatternService cvTextPatternService) {
        this.anthropicChatModel = anthropicChatModel;
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
            You are a professional CV writer. Tailor this professional summary for a specific job application.
            
            Original Summary:
            %s
            
            Job Details:
            - Title: %s
            - Company: %s
            - Description: %s
            
            Requirements:
            1. Keep the same professional tone and structure
            2. Emphasize skills and experience most relevant to this specific job
            3. Maintain authenticity - don't add fake experience
            4. Keep it concise (3-4 lines maximum)
            5. Use keywords from the job description naturally
            
            Return only the tailored summary, no explanations or additional text.
            """, originalSummary, jobOffer.getTitle(), jobOffer.getCompany(), jobOffer.getDescription());
        
        return callClaude(prompt);
    }
    
    private String tailorExperience(String originalExperience, JobOffer jobOffer) {
        String prompt = String.format("""
            You are a professional CV writer. Tailor this work experience section for a specific job application.
            
            Original Experience:
            %s
            
            Job Details:
            - Title: %s
            - Company: %s
            - Description: %s
            
            Requirements:
            1. Reorder and emphasize bullet points most relevant to this job
            2. Use keywords from the job description in the bullet points
            3. Quantify achievements where possible
            4. Keep all experience factual - don't invent new accomplishments
            5. Maintain the same job titles and companies
            6. Focus on technologies and skills mentioned in the job posting
            
            Return only the tailored experience section, preserving the original structure and formatting.
            """, originalExperience, jobOffer.getTitle(), jobOffer.getCompany(), jobOffer.getDescription());
        
        return callClaude(prompt);
    }
    
    private Map<String, String> tailorSkills(String originalSkills, JobOffer jobOffer) {
        String prompt = String.format("""
            You are a professional CV writer. Tailor this skills section for a specific job application.
            
            Original Skills:
            %s
            
            Job Details:
            - Title: %s
            - Company: %s
            - Description: %s
            
            Requirements:
            1. Prioritize and reorder skills most relevant to this job
            2. Keep the same category structure (Frontend, Backend, etc.)
            3. Only include skills that are actually listed in the original
            4. Don't add new technologies or skills
            5. Emphasize technologies mentioned in the job description
            
            Return the skills in this exact JSON format:
            {
                "Frontend": "skill1, skill2, skill3...",
                "Backend": "skill1, skill2, skill3...",
                "Architecture & Patterns": "skill1, skill2, skill3...",
                "DevOps & Tools": "skill1, skill2, skill3...",
                "Testing & Quality": "skill1, skill2, skill3..."
            }
            """, originalSkills, jobOffer.getTitle(), jobOffer.getCompany(), jobOffer.getDescription());
        
        String response = callClaude(prompt);
        
        Map<String, String> skillsMap = new HashMap<>();
        skillsMap.put("skills_response", response);
        return skillsMap;
    }
    
    private String callClaude(String prompt) {
        try {
            UserMessage userMessage = new UserMessage(prompt);
            Prompt claudePrompt = new Prompt(userMessage);
            ChatResponse response = anthropicChatModel.call(claudePrompt);
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("Error calling Claude API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get response from Claude", e);
        }
    }
}