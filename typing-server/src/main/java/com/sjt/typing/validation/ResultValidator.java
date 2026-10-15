package com.sjt.typing.validation;

import com.sjt.typing.dto.req.SubmitResultReq;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 成绩反作弊校验器。
 *
 * <p>核心前提：服务端拿不到完整击键流，无法重算 WPM，因此不是「重算验证」，
 * 而是对前端提交的多个指标做<b>交叉一致性校验</b>——伪造者要让 WPM、字符统计、
 * 每秒采样、键盘节奏分布同时自洽，成本远高于收益。</p>
 *
 * <p>三级处置：全部通过 validity=1；命中「待审」项 validity=0（进人工复核队列）；
 * 命中「拒绝」项 validity=-1（落库留证但不计成绩）。</p>
 */
@Component
public class ResultValidator {

    /** 拒绝类阈值：反算 WPM 与提交值的最大相对偏差 */
    private static final double WPM_MISMATCH_RATIO = 0.30;
    /** 拒绝类阈值：time 模式下 testDuration 与 mode2 的允许偏差（秒） */
    private static final double TIME_TOLERANCE_SECONDS = 1.0;

    private final ResultValidationProperties props;

    public ResultValidator(ResultValidationProperties props) {
        this.props = props;
    }

    /**
     * 校验入口：依次执行各条规则并汇总。
     */
    public ValidationOutcome validate(SubmitResultReq req) {
        List<String> rejectFlags = new ArrayList<>();
        List<String> reviewFlags = new ArrayList<>();

        // ---- 拒绝类规则 ----
        if (checkTimeConsistency(req)) {
            rejectFlags.add("TIME_MISMATCH");
        }
        if (checkTimestampNotFuture(req)) {
            rejectFlags.add("TIMESTAMP_FUTURE");
        }
        if (checkWpmWithinMax(req)) {
            rejectFlags.add("WPM_TOO_HIGH");
        }
        if (checkRawWpmNotBelowWpm(req)) {
            rejectFlags.add("RAW_WPM_LT_WPM");
        }
        if (checkAccRange(req)) {
            rejectFlags.add("ACC_OUT_OF_RANGE");
        }
        if (checkPerfectAccHasNoError(req)) {
            rejectFlags.add("PERFECT_ACC_HAS_ERRORS");
        }
        if (checkCharStatsWpmConsistency(req)) {
            rejectFlags.add("CHAR_STATS_WPM_MISMATCH");
        }

        // ---- 待审类规则 ----
        if (checkKeySpacingTooUniform(req)) {
            reviewFlags.add("KEY_SPACING_UNIFORM");
        }
        if (checkAfkRatio(req)) {
            reviewFlags.add("AFK_RATIO_HIGH");
        }
        if (checkChartErrMismatch(req)) {
            reviewFlags.add("CHART_ERR_MISMATCH");
        }

        List<String> allFlags = new ArrayList<>(rejectFlags);
        allFlags.addAll(reviewFlags);

        if (!rejectFlags.isEmpty()) {
            return new ValidationOutcome(-1, allFlags);
        }
        if (!reviewFlags.isEmpty()) {
            return new ValidationOutcome(0, allFlags);
        }
        return new ValidationOutcome(1, new ArrayList<>());
    }

    // =========================================================================
    // 拒绝类规则
    // =========================================================================

    /**
     * 时间一致性：time 模式下 testDuration 必须与 mode2（秒数）匹配。
     * 防作弊原理：15 秒模式不可能产生 900 秒用时——直接篡改时长会在此暴露。
     */
    boolean checkTimeConsistency(SubmitResultReq req) {
        if (!"time".equals(req.getMode())) {
            return false;
        }
        if (req.getTestDuration() == null) {
            return false;
        }
        Double expected = parseSeconds(req.getMode2());
        if (expected == null) {
            return false;
        }
        double diff = Math.abs(req.getTestDuration().doubleValue() - expected);
        return diff > TIME_TOLERANCE_SECONDS;
    }

    /**
     * 时间戳合理性：clientTimestamp 不得晚于服务端当前时间。
     * 防作弊原理：未来时间戳意味着伪造或时钟篡改，也用于防重放排序造假。
     */
    boolean checkTimestampNotFuture(SubmitResultReq req) {
        Long ts = req.getClientTimestamp();
        if (ts == null) {
            return false;
        }
        return ts > System.currentTimeMillis();
    }

    /**
     * WPM 上限：超过人类极限阈值（可配置，默认 350）即拒绝。
     * 防作弊原理：这是物理不可能值，无需交叉验证即可判定。
     */
    boolean checkWpmWithinMax(SubmitResultReq req) {
        BigDecimal wpm = req.getWpm();
        if (wpm == null) {
            return false;
        }
        return wpm.doubleValue() > props.getMaxWpm();
    }

    /**
     * rawWpm >= wpm：原始速度不得低于净速度。
     * 防作弊原理：rawWpm 含错误击键，数学上必定 >= 净 wpm；违反即数据被篡改。
     */
    boolean checkRawWpmNotBelowWpm(SubmitResultReq req) {
        if (req.getWpm() == null || req.getRawWpm() == null) {
            return false;
        }
        return req.getRawWpm().compareTo(req.getWpm()) < 0;
    }

    /**
     * 准确率边界：acc 必须落在 [0,100]。
     * 防作弊原理：准确率是百分比，越界即为非法数据。
     */
    boolean checkAccRange(SubmitResultReq req) {
        BigDecimal acc = req.getAcc();
        if (acc == null) {
            return false;
        }
        return acc.doubleValue() < 0 || acc.doubleValue() > 100;
    }

    /**
     * 准确率为 100% 时不应存在 incorrect/extra 击键。
     * 防作弊原理：满分准确率与错误字符统计自相矛盾，二者不可能同时成立。
     */
    boolean checkPerfectAccHasNoError(SubmitResultReq req) {
        BigDecimal acc = req.getAcc();
        if (acc == null || acc.doubleValue() != 100.0) {
            return false;
        }
        Map<String, Integer> stats = req.getCharStats();
        if (stats == null) {
            return false;
        }
        int incorrect = intOf(stats, "incorrect");
        int extra = intOf(stats, "extra");
        return incorrect > 0 || extra > 0;
    }

    /**
     * charStats 与 WPM 自洽：由 correct 字符数与 testDuration 反算的 WPM 与提交值偏差超阈值。
     * 防作弊原理：WPM = (correct/5) / (分钟数)，这是硬公式；只改 wpm 而不改 charStats
     * 会导致反算值与提交值严重不符。
     */
    boolean checkCharStatsWpmConsistency(SubmitResultReq req) {
        if (req.getCharStats() == null || req.getTestDuration() == null || req.getWpm() == null) {
            return false;
        }
        double durationSeconds = req.getTestDuration().doubleValue();
        double wpm = req.getWpm().doubleValue();
        if (durationSeconds <= 0 || wpm <= 0) {
            return false;
        }
        int correct = intOf(req.getCharStats(), "correct");
        // WPM = (correct / 5) / (durationSeconds / 60)
        double derivedWpm = (correct / 5.0) / (durationSeconds / 60.0);
        double deviation = Math.abs(derivedWpm - wpm) / wpm;
        return deviation > WPM_MISMATCH_RATIO;
    }

    // =========================================================================
    // 待审类规则
    // =========================================================================

    /**
     * 键盘节奏离群：keySpacingStats.sd 过小意味着机器级恒定间隔。
     * 防作弊原理：人类击键间隔必然存在抖动；标准差趋近 0 通常来自脚本固定延迟。
     */
    boolean checkKeySpacingTooUniform(SubmitResultReq req) {
        Map<String, Object> spacing = req.getKeySpacingStats();
        if (spacing == null) {
            return false;
        }
        Double sd = numberOf(spacing, "sd");
        if (sd == null) {
            return false;
        }
        return sd < props.getMinConsistencySdMs();
    }

    /**
     * AFK 检测：挂机时长占测试总时长比例超阈值却仍有成绩。
     * 防作弊原理：正常测试不会长时间无输入；高挂机比例暗示自动脚本或伪造时长。
     */
    boolean checkAfkRatio(SubmitResultReq req) {
        if (req.getAfkDuration() == null || req.getTestDuration() == null) {
            return false;
        }
        double totalMs = req.getTestDuration().doubleValue() * 1000.0;
        if (totalMs <= 0) {
            return false;
        }
        double ratio = req.getAfkDuration() / totalMs;
        return ratio > props.getMaxAfkRatio();
    }

    /**
     * 采样与总账自洽：chartData 累计 err 数与 charStats 的 incorrect+extra 偏差超阈值。
     * 防作弊原理：每秒采样与字符总账是两条独立数据路径，伪造者很难同时改对。
     */
    boolean checkChartErrMismatch(SubmitResultReq req) {
        List<Map<String, Object>> chart = req.getChartData();
        Map<String, Integer> stats = req.getCharStats();
        if (chart == null || stats == null || chart.isEmpty()) {
            return false;
        }
        long chartErr = 0;
        for (Map<String, Object> point : chart) {
            if (point == null) {
                continue;
            }
            Double err = numberOf(point, "err");
            if (err != null) {
                chartErr += err.longValue();
            }
        }
        long statsErr = intOf(stats, "incorrect") + intOf(stats, "extra");
        if (statsErr == 0 && chartErr == 0) {
            return false;
        }
        long denom = Math.max(statsErr, 1L);
        double deviation = Math.abs(chartErr - statsErr) / (double) denom;
        return deviation > props.getChartToleranceRatio();
    }

    // =========================================================================
    // 工具方法
    // =========================================================================

    /** 解析 mode2 为秒数，非法返回 null */
    private Double parseSeconds(String mode2) {
        if (mode2 == null) {
            return null;
        }
        try {
            return Double.parseDouble(mode2.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 从 Map 取 int，缺失或非法返回 0 */
    private int intOf(Map<String, Integer> map, String key) {
        Integer v = map.get(key);
        return v == null ? 0 : v;
    }

    /** 从 Map 取 double（兼容 Number 与字符串），缺失返回 null */
    private Double numberOf(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 校验结果：validity（1 有效 / 0 待审 / -1 拒绝）与命中的 flags。
     */
    public static class ValidationOutcome {

        private final int validity;
        private final List<String> flags;

        public ValidationOutcome(int validity, List<String> flags) {
            this.validity = validity;
            this.flags = flags;
        }

        public int getValidity() {
            return validity;
        }

        public List<String> getFlags() {
            return flags;
        }
    }
}
