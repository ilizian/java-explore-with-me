package ru.practicum.explore.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/admin")
public class AdminController {
    private final EventService eventService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final CompilationService compilationService;
    private final CommentService commentService;

    @Autowired
    public AdminController(UserService userService,
                           EventService eventService,
                           CompilationService compilationService,
                           CategoryService categoryService,
                           CommentService commentService) {
        this.userService = userService;
        this.eventService = eventService;
        this.compilationService = compilationService;
        this.categoryService = categoryService;
        this.commentService = commentService;
    }

    @GetMapping("/users")
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(required = false, defaultValue = "0") Integer from,
                                  @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET. Получить список пользователей");
        return userService.getUsers(ids, from, size);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody @Valid NewUserRequest newUserDto) {
        log.info("POST. Создать пользователя " + newUserDto.getEmail());
        return userService.addUser(newUserDto);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE. Удалить пользователя по id " + userId);
        userService.deleteUser(userId);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid NewCategoryDto categoryDto) {
        log.info("POST. Создать категорию " + categoryDto.getName());
        return categoryService.addCategory(categoryDto);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("DELETE. Удалить категорию по id " + catId);
        categoryService.deleteCategoryById(catId);
    }

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilationDto compilationDto) {
        log.info("POST. Создать событие " + compilationDto.getTitle());
        return compilationService.addCompilation(compilationDto);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @RequestBody @Valid UpdateCompilationRequest compilationDto) {
        log.info("PATCH. Изменить событие по id " + compId);
        return compilationService.updateCompilation(compId, compilationDto);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("DELETE. Удалить событие по id " + compId);
        compilationService.deleteCompilationById(compId);
    }

    @PatchMapping("/categories/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @RequestBody @Valid CategoryDto categoryDto) throws ValidationException {
        log.info("PATCH. Изменить категорию по id " + catId);
        return categoryService.updateCategory(catId, categoryDto);
    }

    @GetMapping("/events")
    public List<EventFullDto> getEvents(@RequestParam(required = false) List<Long> users,
                                        @RequestParam(required = false) List<String> states,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false) String rangeStart,
                                        @RequestParam(required = false) String rangeEnd,
                                        @RequestParam(required = false, defaultValue = "0") Integer from,
                                        @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET. Получить события");
        return eventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) throws ValidationException {
        log.info("PATCH. Изменить событие по id " + eventId);
        return eventService.updateEvent(eventId, updateEventAdminRequest);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("DELETE. Удаление комментария по id " + commentId);
        commentService.deleteComment(commentId);
    }
}