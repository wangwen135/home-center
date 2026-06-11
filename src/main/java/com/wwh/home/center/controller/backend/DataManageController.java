package com.wwh.home.center.controller.backend;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwh.home.center.common.exception.ForbiddenException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.dao.mapper.FamousQuotesMapper;
import com.wwh.home.center.dao.mapper.InternalSystemConfigMapper;
import com.wwh.home.center.dao.mapper.PromptMessageMapper;
import com.wwh.home.center.model.entity.FamousQuotes;
import com.wwh.home.center.model.entity.InternalSystemConfig;
import com.wwh.home.center.model.entity.OperationLog;
import com.wwh.home.center.model.entity.PromptMessage;
import com.wwh.home.center.model.entity.SecurityLog;
import com.wwh.home.center.model.entity.SysLog;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.FamousQuotesService;
import com.wwh.home.center.service.InternalSystemConfigService;
import com.wwh.home.center.service.OperationLogService;
import com.wwh.home.center.service.PromptMessageService;
import com.wwh.home.center.service.SecurityLogService;
import com.wwh.home.center.service.SysLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Api(tags = "后台数据管理接口")
@Validated
@RestController
@RequestMapping("/backend/data")
public class DataManageController {

    @Autowired
    private FamousQuotesService famousQuotesService;

    @Autowired
    private PromptMessageService promptMessageService;

    @Autowired
    private InternalSystemConfigService internalSystemConfigService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private SecurityLogService securityLogService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private FamousQuotesMapper famousQuotesMapper;

    @Autowired
    private PromptMessageMapper promptMessageMapper;

    @Autowired
    private InternalSystemConfigMapper internalSystemConfigMapper;

    @ApiOperation("名言列表")
    @GetMapping("/famous-quotes")
    public Result<List<FamousQuotes>> famousQuotes() {
        checkSuperAdmin();
        return Result.success(famousQuotesService.getAllFamous());
    }

    @ApiOperation("新增名言")
    @PostMapping("/famous-quote")
    public Result<Void> addFamousQuote(@RequestBody FamousQuotes famousQuotes) {
        checkSuperAdmin();
        famousQuotes.setId(null);
        famousQuotes.setDeleted(false);
        famousQuotesMapper.insert(famousQuotes);
        return Result.success();
    }

    @ApiOperation("修改名言")
    @PutMapping("/famous-quote")
    public Result<Void> updateFamousQuote(@RequestBody FamousQuotes famousQuotes) {
        checkSuperAdmin();
        famousQuotesMapper.updateById(famousQuotes);
        return Result.success();
    }

    @ApiOperation("删除名言")
    @DeleteMapping("/famous-quote/{id}")
    public Result<Void> deleteFamousQuote(@PathVariable @NotNull(message = "名言ID不能为空") Integer id) {
        checkSuperAdmin();
        FamousQuotes famousQuotes = new FamousQuotes();
        famousQuotes.setId(id);
        famousQuotes.setDeleted(true);
        famousQuotesMapper.updateById(famousQuotes);
        return Result.success();
    }

    @ApiOperation("提示消息列表")
    @GetMapping("/prompt-messages")
    public Result<List<PromptMessage>> promptMessages() {
        checkSuperAdmin();
        QueryWrapper<PromptMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false).orderByDesc("weight").orderByDesc("id");
        return Result.success(promptMessageMapper.selectList(queryWrapper));
    }

    @ApiOperation("新增提示消息")
    @PostMapping("/prompt-message")
    public Result<Void> addPromptMessage(@RequestBody PromptMessage promptMessage) {
        checkSuperAdmin();
        promptMessage.setId(null);
        promptMessage.setDeleted(false);
        promptMessageMapper.insert(promptMessage);
        return Result.success();
    }

    @ApiOperation("修改提示消息")
    @PutMapping("/prompt-message")
    public Result<Void> updatePromptMessage(@RequestBody PromptMessage promptMessage) {
        checkSuperAdmin();
        promptMessageMapper.updateById(promptMessage);
        return Result.success();
    }

    @ApiOperation("删除提示消息")
    @DeleteMapping("/prompt-message/{id}")
    public Result<Void> deletePromptMessage(@PathVariable @NotNull(message = "提示消息ID不能为空") Integer id) {
        checkSuperAdmin();
        PromptMessage promptMessage = new PromptMessage();
        promptMessage.setId(id);
        promptMessage.setDeleted(true);
        promptMessageMapper.updateById(promptMessage);
        return Result.success();
    }

    @ApiOperation("内部系统配置列表")
    @GetMapping("/internal-systems")
    public Result<List<InternalSystemConfig>> internalSystems() {
        checkSuperAdmin();
        return Result.success(internalSystemConfigService.getAll());
    }

    @ApiOperation("新增内部系统配置")
    @PostMapping("/internal-system")
    public Result<Void> addInternalSystem(@RequestBody InternalSystemConfig internalSystemConfig) {
        checkSuperAdmin();
        internalSystemConfig.setId(null);
        internalSystemConfig.setDeleted(false);
        internalSystemConfigMapper.insert(internalSystemConfig);
        return Result.success();
    }

    @ApiOperation("修改内部系统配置")
    @PutMapping("/internal-system")
    public Result<Void> updateInternalSystem(@RequestBody InternalSystemConfig internalSystemConfig) {
        checkSuperAdmin();
        internalSystemConfigMapper.updateById(internalSystemConfig);
        return Result.success();
    }

    @ApiOperation("删除内部系统配置")
    @DeleteMapping("/internal-system/{id}")
    public Result<Void> deleteInternalSystem(@PathVariable @NotNull(message = "内部系统ID不能为空") Integer id) {
        checkSuperAdmin();
        InternalSystemConfig internalSystemConfig = new InternalSystemConfig();
        internalSystemConfig.setId(id);
        internalSystemConfig.setDeleted(true);
        internalSystemConfigMapper.updateById(internalSystemConfig);
        return Result.success();
    }

    @ApiOperation("操作日志列表")
    @GetMapping("/operation-logs")
    public Result<List<OperationLog>> operationLogs() {
        checkSuperAdmin();
        return Result.success(operationLogService.listAll());
    }

    @ApiOperation("安全日志列表")
    @GetMapping("/security-logs")
    public Result<List<SecurityLog>> securityLogs() {
        checkSuperAdmin();
        return Result.success(securityLogService.listAll());
    }

    @ApiOperation("系统日志列表")
    @GetMapping("/sys-logs")
    public Result<List<SysLog>> sysLogs() {
        checkSuperAdmin();
        return Result.success(sysLogService.listAll());
    }

    private void checkSuperAdmin() {
        if (!UserContextHolder.isSuperAdmin()) {
            throw new ForbiddenException("只有超级管理员才能访问后台管理接口");
        }
    }
}
