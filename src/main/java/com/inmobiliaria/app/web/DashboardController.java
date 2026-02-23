package com.inmobiliaria.app.web;

import com.inmobiliaria.app.domain.*;
import com.inmobiliaria.app.repo.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
public class DashboardController {

    private final AppUserRepository userRepository;
    private final VisitRepository visitRepository;
    private final AgendaNoteRepository noteRepository;
    private final ClientPropertyInteractionRepository interactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DashboardController(AppUserRepository userRepository,
                               VisitRepository visitRepository,
                               AgendaNoteRepository noteRepository,
                               ClientPropertyInteractionRepository interactionRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository        = userRepository;
        this.visitRepository       = visitRepository;
        this.noteRepository        = noteRepository;
        this.interactionRepository = interactionRepository;
        this.passwordEncoder       = passwordEncoder;
    }

    // ── GET /login ───────────────────────────────────────────
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ── GET /dashboard ───────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(value = "agendaDate", required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate agendaDate,
                            Model model) {

        String username = userDetails.getUsername();
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        LocalDate today      = LocalDate.now();
        LocalDateTime start  = today.atStartOfDay();
        LocalDateTime end    = today.atTime(LocalTime.MAX);

        // Visitas de hoy
        List<Visit> todayVisits = visitRepository
                .findByStatusAndVisitAtBetweenOrderByVisitAtAsc(
                        VisitStatus.PROGRAMADA, start, end);

        // Notas agenda (filtradas por fecha o todas)
        List<AgendaNote> notes = agendaDate != null
                ? noteRepository.findByUsernameAndNoteDateOrderByNoteDateAsc(username, agendaDate)
                : noteRepository.findByUsernameOrderByNoteDateAsc(username);

        // Top 10 inmuebles por interacciones
        List<Object[]> topProps = interactionRepository
                .findTopPropertiesByInteractionCount(PageRequest.of(0, 10));

        model.addAttribute("appUser",     appUser);
        model.addAttribute("today",       today);
        model.addAttribute("todayVisits", todayVisits);
        model.addAttribute("notes",       notes);
        model.addAttribute("agendaDate",  agendaDate);
        model.addAttribute("topProps",    topProps);
        model.addAttribute("allUsers",    userRepository.findAll());
        return "dashboard";
    }

    // ── POST /agenda/nueva ───────────────────────────────────
    @PostMapping("/agenda/nueva")
    public String newNote(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam("noteDate")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate noteDate,
                          @RequestParam("content") String content) {

        AgendaNote note = new AgendaNote();
        note.setUsername(userDetails.getUsername());
        note.setNoteDate(noteDate);
        note.setContent(content.trim());
        noteRepository.save(note);
        return "redirect:/dashboard";
    }

    // ── POST /agenda/{id}/eliminar ───────────────────────────
    @PostMapping("/agenda/{id}/eliminar")
    @ResponseBody
    public void deleteNote(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails) {
        AgendaNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!note.getUsername().equals(userDetails.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        noteRepository.deleteById(id);
    }

    // ── POST /usuarios/registrar (solo ADMIN) ────────────────
    @PostMapping("/usuarios/registrar")
    public String registerUser(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("username")    String username,
                               @RequestParam("password")    String password,
                               @RequestParam("displayName") String displayName) {

        if (userDetails.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        AppUser u = new AppUser();
        u.setUsername(username.trim());
        u.setPassword(passwordEncoder.encode(password));
        u.setDisplayName(displayName.trim());
        u.setRole("USER");
        userRepository.save(u);
        return "redirect:/dashboard";
    }

    // ── POST /usuarios/{id}/eliminar (solo ADMIN) ────────────
    @PostMapping("/usuarios/{id}/eliminar")
    @ResponseBody
    public void deleteUser(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        userRepository.deleteById(id);
    }
}
