package com.yucairoad.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yucairoad.common.BusinessException;
import com.yucairoad.dto.GameState;
import com.yucairoad.entity.GameSave;
import com.yucairoad.mapper.GameSaveMapper;
import com.yucairoad.service.GameEngineService;
import com.yucairoad.service.GameSaveService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class GameEngineServiceImpl implements GameEngineService {

    private static final BigDecimal TUITION_PER_STUDENT = new BigDecimal("6000");
    private static final BigDecimal GOVERNMENT_GRANT = new BigDecimal("50000");
    private static final BigDecimal TEACHER_BASE_SALARY = new BigDecimal("8000");
    private static final BigDecimal STUDENT_SUBSIDY = new BigDecimal("200");
    private static final BigDecimal BUILDING_MAINTENANCE_BASE = new BigDecimal("5000");

    private final GameSaveService gameSaveService;
    private final GameSaveMapper gameSaveMapper;
    private final ObjectMapper objectMapper;

    public GameEngineServiceImpl(GameSaveService gameSaveService,
                                 GameSaveMapper gameSaveMapper) {
        this.gameSaveService = gameSaveService;
        this.gameSaveMapper = gameSaveMapper;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public GameState advanceMonth(Long saveId) {
        GameState state = loadGameState(saveId);

        int currentMonth = state.getCurrentMonth();
        int nextMonth = currentMonth + 1;
        if (nextMonth > 12) {
            nextMonth = 1;
            state.setCurrentYear(state.getCurrentYear() + 1);
        }
        state.setCurrentMonth(nextMonth);

        processKeyMonthEvents(state, nextMonth);
        calculateMonthlyFinance(state);
        persistGameState(saveId, state);

        return state;
    }

    @Override
    public GameState getGameState(Long saveId) {
        return loadGameState(saveId);
    }

    @Override
    public GameState setSpeed(Long saveId, int speed) {
        if (speed != 1 && speed != 2 && speed != 5 && speed != 10) {
            throw new BusinessException("无效的加速倍率，仅支持 1/2/5/10");
        }
        GameState state = loadGameState(saveId);
        state.setSpeed(speed);
        persistGameState(saveId, state);
        return state;
    }

    @Override
    public GameState togglePause(Long saveId) {
        GameState state = loadGameState(saveId);
        state.setIsPaused(!state.getIsPaused());
        persistGameState(saveId, state);
        return state;
    }

    private void processKeyMonthEvents(GameState state, int month) {
        List<GameState.GameEvent> events = state.getEvents();
        if (events == null) {
            events = new ArrayList<>();
            state.setEvents(events);
        }

        switch (month) {
            case 9:
                if (state.getSchool() == null) {
                    initializeNewSchool(state);
                }
                addEvent(events, "ENROLLMENT", "开学招生", "新学年招生季开始，欢迎新同学加入" + getSchoolName(state), month);
                break;
            case 1:
                addEvent(events, "EXAM_FINAL", "期末考试", "期末考试月，检验本学期教学成果", month);
                break;
            case 3:
                addEvent(events, "SEMESTER_START", "春季开学", "新学期开始，师生返校", month);
                break;
            case 6:
                addEvent(events, "EXAM_MAJOR", "大考来临", "高考、中考、小升初等重要考试即将到来", month);
                break;
            case 8:
                addEvent(events, "YEAR_SETTLEMENT", "学年结算", "学年度结算完成，学校发展进入新阶段", month);
                if (state.getSchool() != null) {
                    state.getSchool().setTotalYears(
                            (state.getSchool().getTotalYears() == null ? 0 : state.getSchool().getTotalYears()) + 1
                    );
                }
                break;
            default:
                break;
        }

        state.setEvents(events);
    }

    private void initializeNewSchool(GameState state) {
        GameState.SchoolInfo schoolInfo = new GameState.SchoolInfo();
        schoolInfo.setName("育才中学");
        schoolInfo.setType("高中");
        schoolInfo.setLevel("普通");
        schoolInfo.setTotalYears(0);

        List<GameState.BuildingInfo> buildings = new ArrayList<>();

        GameState.BuildingInfo teachingBuilding = new GameState.BuildingInfo();
        teachingBuilding.setType("教学楼");
        teachingBuilding.setLevel(1);
        teachingBuilding.setCapacity(300);
        teachingBuilding.setMonthlyCost(new BigDecimal("2000"));
        buildings.add(teachingBuilding);

        GameState.BuildingInfo dormitory = new GameState.BuildingInfo();
        dormitory.setType("宿舍楼");
        dormitory.setLevel(1);
        dormitory.setCapacity(200);
        dormitory.setMonthlyCost(new BigDecimal("1500"));
        buildings.add(dormitory);

        GameState.BuildingInfo library = new GameState.BuildingInfo();
        library.setType("图书馆");
        library.setLevel(1);
        library.setCapacity(100);
        library.setMonthlyCost(new BigDecimal("1000"));
        buildings.add(library);

        schoolInfo.setBuildings(buildings);

        List<GameState.TeacherInfo> teachers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            GameState.TeacherInfo teacher = new GameState.TeacherInfo();
            teacher.setName("教师" + i);
            teacher.setLevel("二级教师");
            teacher.setTeachingAbility(60 + (int)(Math.random() * 20));
            teacher.setSalary(TEACHER_BASE_SALARY);
            teachers.add(teacher);
        }
        schoolInfo.setTeachers(teachers);

        state.setSchool(schoolInfo);
        state.setTeacherCount(10);
        state.setStudentCount(0);
    }

    private void calculateMonthlyFinance(GameState state) {
        int studentCount = state.getStudentCount() == null ? 0 : state.getStudentCount();
        int teacherCount = state.getTeacherCount() == null ? 0 : state.getTeacherCount();

        BigDecimal income = TUITION_PER_STUDENT.multiply(BigDecimal.valueOf(studentCount))
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
                .add(GOVERNMENT_GRANT.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));

        BigDecimal teacherSalary = TEACHER_BASE_SALARY.multiply(BigDecimal.valueOf(teacherCount));
        BigDecimal studentSubsidyTotal = STUDENT_SUBSIDY.multiply(BigDecimal.valueOf(studentCount));
        BigDecimal buildingMaintenance = BUILDING_MAINTENANCE_BASE;

        if (state.getSchool() != null && state.getSchool().getBuildings() != null) {
            for (GameState.BuildingInfo building : state.getSchool().getBuildings()) {
                if (building.getMonthlyCost() != null) {
                    buildingMaintenance = buildingMaintenance.add(building.getMonthlyCost());
                }
            }
        }

        BigDecimal expense = teacherSalary.add(studentSubsidyTotal).add(buildingMaintenance);
        BigDecimal netIncome = income.subtract(expense);

        BigDecimal currentFunds = state.getFunds() == null ? BigDecimal.ZERO : state.getFunds();
        state.setFunds(currentFunds.add(netIncome));
    }

    private void addEvent(List<GameState.GameEvent> events, String type, String title, String description, int month) {
        GameState.GameEvent event = new GameState.GameEvent();
        event.setType(type);
        event.setTitle(title);
        event.setDescription(description);
        event.setMonth(month);
        events.add(event);
    }

    private String getSchoolName(GameState state) {
        if (state.getSchool() != null && state.getSchool().getName() != null) {
            return state.getSchool().getName();
        }
        return "学校";
    }

    private GameState loadGameState(Long saveId) {
        GameSave save = gameSaveMapper.selectById(saveId);
        if (save == null) {
            throw new BusinessException("存档不存在");
        }
        String gameStateJson = save.getGameState();
        if (gameStateJson == null || gameStateJson.isBlank()) {
            return createInitialGameState();
        }
        try {
            return objectMapper.readValue(gameStateJson, new TypeReference<GameState>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException("存档数据解析失败");
        }
    }

    private void persistGameState(Long saveId, GameState state) {
        try {
            String json = objectMapper.writeValueAsString(state);
            gameSaveService.updateSave(null, saveId, json);
        } catch (JsonProcessingException e) {
            throw new BusinessException("存档数据保存失败");
        }
    }

    private GameState createInitialGameState() {
        GameState state = new GameState();
        state.setCurrentYear(1);
        state.setCurrentMonth(9);
        state.setFunds(new BigDecimal("2000000"));
        state.setReputation(0);
        state.setStudentCount(0);
        state.setTeacherCount(0);
        state.setSpeed(1);
        state.setIsPaused(false);
        return state;
    }
}
