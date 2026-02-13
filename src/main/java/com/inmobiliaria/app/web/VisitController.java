package com.inmobiliaria.app.web;

import com.inmobiliaria.app.domain.VisitStatus;
import com.inmobiliaria.app.repo.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VisitController {

    private final VisitRepository visitRepository;

    public VisitController(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @GetMapping("/visitas/programadas")
    public String scheduled(Model model) {
        model.addAttribute("visits", visitRepository.findByStatusOrderByVisitAtAsc(VisitStatus.PROGRAMADA));
        return "visits_scheduled";
    }

    @GetMapping("/visitas/realizadas")
    public String done(Model model) {
        model.addAttribute("visits", visitRepository.findByStatusOrderByVisitAtAsc(VisitStatus.REALIZADA));
        return "visits_done";
    }
}
