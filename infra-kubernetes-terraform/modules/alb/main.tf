# ================================
# Application Load Balancer
# ================================
resource "aws_lb" "this" {
  name               = var.alb_name
  internal           = var.internal
  load_balancer_type = "application"
  security_groups    = var.security_groups
  subnets            = var.subnets

  enable_deletion_protection = var.enable_deletion_protection
  enable_http2              = var.enable_http2
  enable_cross_zone_load_balancing = var.enable_cross_zone_load_balancing

  idle_timeout = var.idle_timeout

  dynamic "access_logs" {
    for_each = var.access_logs_enabled ? [1] : []
    content {
      bucket  = var.access_logs_bucket
      prefix  = var.access_logs_prefix
      enabled = true
    }
  }

  tags = merge(
    var.tags,
    {
      Name = var.alb_name
    }
  )
}

# ================================
# Target Group
# ================================
resource "aws_lb_target_group" "this" {
  name        = var.target_group_name
  port        = var.target_port
  protocol    = var.target_protocol
  vpc_id      = var.vpc_id
  target_type = "ip" # ECS Fargate usa IP

  health_check {
    enabled             = true
    healthy_threshold   = var.health_check_healthy_threshold
    unhealthy_threshold = var.health_check_unhealthy_threshold
    timeout             = var.health_check_timeout
    interval            = var.health_check_interval
    path                = var.health_check_path
    protocol            = var.health_check_protocol
    matcher             = var.health_check_matcher
  }

  deregistration_delay = var.deregistration_delay

  stickiness {
    type            = "lb_cookie"
    enabled         = var.stickiness_enabled
    cookie_duration = var.stickiness_duration
  }

  tags = merge(
    var.tags,
    {
      Name = var.target_group_name
    }
  )
}

# ================================
# HTTP Listener (Redirect to HTTPS)
# ================================
resource "aws_lb_listener" "http" {
  count             = var.enable_https ? 1 : 0
  load_balancer_arn = aws_lb.this.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

# ================================
# HTTPS Listener
# ================================
resource "aws_lb_listener" "https" {
  count             = var.enable_https ? 1 : 0
  load_balancer_arn = aws_lb.this.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = var.ssl_policy
  certificate_arn   = var.certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.this.arn
  }
}

# ================================
# HTTP Listener (No HTTPS)
# ================================
resource "aws_lb_listener" "http_only" {
  count             = var.enable_https ? 0 : 1
  load_balancer_arn = aws_lb.this.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.this.arn
  }
}

# ================================
# Listener Rules (Optional)
# ================================
resource "aws_lb_listener_rule" "path_based" {
  for_each = var.listener_rules

  listener_arn = var.enable_https ? aws_lb_listener.https[0].arn : aws_lb_listener.http_only[0].arn
  priority     = each.value.priority

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.this.arn
  }

  condition {
    path_pattern {
      values = each.value.path_patterns
    }
  }
}
