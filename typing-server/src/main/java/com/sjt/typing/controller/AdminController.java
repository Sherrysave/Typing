package com.sjt.typing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sjt.typing.aop.OpLog;
import com.sjt.typing.common.R;
import com.sjt.typing.dto.req.AdminUidReq;
import com.sjt.typing.dto.req.BanReq;
import com.sjt.typing.dto.req.HandleReportReq;
import com.sjt.typing.dto.req.PsaReq;
import com.sjt.typing.dto.vo.AdminUidVO;
import com.sjt.typing.dto.vo.BlocklistVO;
import com.sjt.typing.dto.vo.LogVO;
import com.sjt.typing.dto.vo.NewQuoteVO;
import com.sjt.typing.dto.vo.PsaVO;
import com.sjt.typing.dto.vo.QuoteVO;
import com.sjt.typing.dto.vo.ReportVO;
import com.sjt.typing.security.UserContext;
import com.sjt.typing.service.AdminService;
import com.sjt.typing.service.QuoteService;
import com.sjt.typing.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理接口（对标 api/routes/admin）。
 *
 * <p>访问控制由 AdminAuthInterceptor 在 {@code /api/admin/**} 上统一实施（Deny by default），
 * 因此这里不写任何权限判断；所有会改变状态的动作都标注 {@link OpLog}，成功执行后自动写入 t_log。</p>
 */
@Tag(name = "后台管理", description = "名言审核 / 封禁 / 举报处理 / 公告 / 日志（需管理员）")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final QuoteService quoteService;
    private final ReportService reportService;
    private final AdminService adminService;

    public AdminController(QuoteService quoteService,
                           ReportService reportService,
                           AdminService adminService) {
        this.quoteService = quoteService;
        this.reportService = reportService;
        this.adminService = adminService;
    }

    // =========================================================================
    // 名言审核流
    // =========================================================================

    @Operation(summary = "分页查询待审名言")
    @GetMapping("/quotes/pending")
    public R<Page<NewQuoteVO>> pendingQuotes(@RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return R.ok(quoteService.listPending(status, page, size));
    }

    @Operation(summary = "通过审核（写入名言库）")
    @OpLog(action = "APPROVE_QUOTE", targetParam = "id")
    @PostMapping("/quotes/{id}/approve")
    public R<QuoteVO> approveQuote(@PathVariable String id) {
        return R.ok(quoteService.approve(id, UserContext.getUidOrThrow()));
    }

    @Operation(summary = "驳回名言")
    @OpLog(action = "REJECT_QUOTE", targetParam = "id")
    @PostMapping("/quotes/{id}/reject")
    public R<Void> rejectQuote(@PathVariable String id) {
        quoteService.reject(id);
        return R.ok();
    }

    // =========================================================================
    // 举报处理
    // =========================================================================

    @Operation(summary = "分页查询举报")
    @GetMapping("/reports")
    public R<Page<ReportVO>> reports(@RequestParam(required = false) Integer status,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return R.ok(reportService.list(status, page, size));
    }

    @Operation(summary = "处理举报（1 已处理 / 2 驳回）")
    @OpLog(action = "HANDLE_REPORT", targetParam = "id")
    @PostMapping("/reports/{id}/handle")
    public R<Void> handleReport(@PathVariable String id, @Valid @RequestBody HandleReportReq req) {
        reportService.handle(id, req.getStatus());
        return R.ok();
    }

    // =========================================================================
    // 封禁与解封
    // =========================================================================

    @Operation(summary = "分页查询封禁名单")
    @GetMapping("/blocklist")
    public R<Page<BlocklistVO>> blocklist(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return R.ok(adminService.blocklist(page, size));
    }

    @Operation(summary = "封禁用户")
    @OpLog(action = "BAN_USER", targetParam = "uid")
    @PostMapping("/users/{uid}/ban")
    public R<Void> ban(@PathVariable String uid, @Valid @RequestBody(required = false) BanReq req) {
        adminService.ban(uid, req == null ? null : req.getReason(), UserContext.getUidOrThrow());
        return R.ok();
    }

    @Operation(summary = "解封用户")
    @OpLog(action = "UNBAN_USER", targetParam = "uid")
    @PostMapping("/users/{uid}/unban")
    public R<Void> unban(@PathVariable String uid) {
        adminService.unban(uid);
        return R.ok();
    }

    // =========================================================================
    // 公告与管理员名单
    // =========================================================================

    @Operation(summary = "发布公告")
    @OpLog(action = "CREATE_PSA")
    @PostMapping("/psas")
    public R<PsaVO> createPsa(@Valid @RequestBody PsaReq req) {
        return R.ok(adminService.createPsa(req));
    }

    @Operation(summary = "分页查询操作日志")
    @GetMapping("/logs")
    public R<Page<LogVO>> logs(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size) {
        return R.ok(adminService.logs(page, size));
    }

    @Operation(summary = "查询管理员名单")
    @GetMapping("/admins")
    public R<List<AdminUidVO>> admins() {
        return R.ok(adminService.admins());
    }

    @Operation(summary = "新增管理员")
    @OpLog(action = "ADD_ADMIN")
    @PostMapping("/admins")
    public R<Void> addAdmin(@Valid @RequestBody AdminUidReq req) {
        adminService.addAdmin(req.getUid(), UserContext.getUidOrThrow());
        return R.ok();
    }

    @Operation(summary = "移除管理员")
    @OpLog(action = "REMOVE_ADMIN", targetParam = "uid")
    @DeleteMapping("/admins/{uid}")
    public R<Void> removeAdmin(@PathVariable String uid) {
        adminService.removeAdmin(uid, UserContext.getUidOrThrow());
        return R.ok();
    }
}
