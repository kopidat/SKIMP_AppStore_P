//
//  SamplePassCodeViewController.m
//

#import "SamplePassCodeViewController.h"

@interface SamplePassCodeViewController () <UIAlertViewDelegate> {
}

@property (nonatomic, retain) IBOutlet UIImageView *indicator1;
@property (nonatomic, retain) IBOutlet UIImageView *indicator2;
@property (nonatomic, retain) IBOutlet UIImageView *indicator3;
@property (nonatomic, retain) IBOutlet UIImageView *indicator4;

@property (nonatomic, retain) IBOutlet UILabel *mainTitleLabel;
@property (nonatomic, retain) IBOutlet UILabel *subTitleLabel;
@property (nonatomic, retain) IBOutlet UILabel *resultMessageLabel;

@property (nonatomic, retain) IBOutlet UIView *lineView;
@property (nonatomic, strong) CAGradientLayer *gradient;

@end

@implementation SamplePassCodeViewController

- (void)loadView {
    [super loadView];
    
    _passmode = PassCodeModeUnset;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.gradient = [CAGradientLayer layer];

    self.gradient.frame = self.lineView.bounds;
    self.gradient.colors = @[(id)[UIColor colorWithHex:@"#FF7A00" alpha:100.0f].CGColor, (id)[UIColor colorWithHex:@"#EA002C" alpha:100.0f].CGColor];

    [self.lineView.layer insertSublayer:self.gradient atIndex:0];
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    
    self.gradient.frame = self.lineView.bounds;
}

- (void)viewWillAppear:(BOOL)animated {
	[super viewWillAppear:animated];
}

- (void)setCodeCount:(NSInteger)codeCount {
    [super setCodeCount:codeCount];
    self.indicator1.highlighted = self.codeCount >= 1 ? YES : NO;
    self.indicator2.highlighted = self.codeCount >= 2 ? YES : NO;
    self.indicator3.highlighted = self.codeCount >= 3 ? YES : NO;
    self.indicator4.highlighted = self.codeCount >= 4 ? YES : NO;
}

- (void)showMode:(NSString *)mode {
    [super showMode:mode];

    if ( [mode isEqualToString:@"REGISTER"] ) {
        [self updateMode:PassCodeRegisterMode];
    }
    else if ( [mode isEqualToString:@"SETTING_REGISTER"] ) {
        [self updateMode:PassCodeRegisterMode];
    }
    else if ( [mode isEqualToString:@"SETTING_CHANGE"] ) {
        [self updateMode:PassCodeChangeMode];
    }
    else if ( [mode isEqualToString:@"LOGIN"] || [mode isEqualToString:@"AUTH"] )
    {
        if([[PassCodeManager defaultManager] hasPassCode])
        {
            _retryCount = 0;
            [self updateMode:PassCodeAuthMode];
        }
        else
        {
            [self authFailAction];
        }
    }
    else
    {
        [self dismiss];
    }
}

- (void)updateMode:(PassCodeMode)mode
{
    _passmode = mode;
    switch (mode) {
        default:
        case PassCodeModeUnset:
            self.labelMessage.text = @"-";
            self.resultMessageLabel.text = @"";
            self.lockedInteraction = NO;
            break;

        case PassCodeRegisterMode:
        case PassCodeRegisterModeNewPassCode:
//            self.labelMessage.text = @"새로운 비밀번호를 입력하십시오";
            self.resultMessageLabel.text = @"";
            self.mainTitleLabel.text = @"간편로그인 설정";
            self.subTitleLabel.text = @"PIN번호 등록";
            self.labelMessage.text = @"사용하실 PIN번호 숫자 4자리를 입력 해 주세요.";
            self.lockedInteraction = NO;
            break;

        case PassCodeRegisterModeConfirmPassCode:
//            self.labelMessage.text = @"비밀번호를 한번 더 입력하십시오";
            self.mainTitleLabel.text = @"간편로그인 설정";
            self.subTitleLabel.text = @"PIN번호 재입력";
            self.labelMessage.text = @"확인을 위해 한번 더 입력 해 주세요.";
            self.resultMessageLabel.text = @"";
            self.lockedInteraction = NO;
            break;

        case PassCodeRegisterModeWrongConfirmPassCode:
//            self.labelMessage.text = @"일지하지 않음. 새로운 비밀번호 다시 입력.";
            self.resultMessageLabel.text = [NSString stringWithFormat:@"입력한 PIN번호가 일치하지 않습니다. (%ld/5)", _retryCount];
            self.lockedInteraction = NO;
            break;

        case PassCodeRegisterModeDone:
            [self passCodeModeEvent:PassCodeRegisterModeDone];
            break;

        case PassCodeChangeModeOldPassCode:
            self.mainTitleLabel.text = @"PIN번호 변경";
            self.subTitleLabel.text = @"이전 PIN번호 입력";
            self.labelMessage.text = @"이전에 사용하셨던/nPIN번호 숫자 4자리를 입력 해 주세요";
            self.resultMessageLabel.text = @"";
            self.lockedInteraction = NO;
            break;

        case PassCodeChangeModeWrongOldPassCode:
//            self.labelMessage.text = @"비밀번호가 일치하지 않습니다";
            self.resultMessageLabel.text = [NSString stringWithFormat:@"입력한 PIN번호가 일치하지 않습니다. (%ld/5)", _retryCount];
            self.lockedInteraction = NO;
            break;

        case PassCodeChangeMode:
        case PassCodeChangeModeNewPassCode:
            self.mainTitleLabel.text = @"PIN번호 변경";
            self.subTitleLabel.text = @"PIN번호 등록";
            self.labelMessage.text = @"사용하실 PIN번호 숫자 4자리를 입력 해 주세요";
            self.resultMessageLabel.text = @"";
            self.lockedInteraction = NO;
            break;

        case PassCodeChangeModeConfirmPassCode:
            self.mainTitleLabel.text = @"PIN번호 변경";
            self.subTitleLabel.text = @"PIN번호 등록";
            self.labelMessage.text = @"확인을 위해 한번 더 입력 해 주세요";
            self.resultMessageLabel.text = @"";
            self.lockedInteraction = NO;
            break;

        case PassCodeChangeModeWrongConfirmPassCode:
//            self.labelMessage.text = @"일지하지 않음. 새로운 비밀번호 다시 입력.";
            self.resultMessageLabel.text = [NSString stringWithFormat:@"입력한 PIN번호가 일치하지 않습니다. (%ld/5)", _retryCount];
            self.lockedInteraction = NO;
            break;

        case PassCodeChangeModeDone:
            [self passCodeModeEvent:PassCodeChangeModeDone];
            break;

        case PassCodeAuthMode:
        case PassCodeAuthModeInput:
            self.mainTitleLabel.text = @"PIN번호 인증";
            self.subTitleLabel.text = @"PIN번호 숫자 4자리를 입력 해 주세요.";
            self.labelMessage.text = @"";
            self.resultMessageLabel.text = @"";
            self.lockedInteraction = NO;
            break;

        case PassCodeAuthModeWrong:
            [self startShakingAnimation];
//            self.labelMessage.text = @"로그인 비밀번호가 일치하지 않습니다";
            self.resultMessageLabel.text = [NSString stringWithFormat:@"입력한 PIN번호가 일치하지 않습니다. (%ld/5)", _retryCount];
            self.lockedInteraction = NO;
            break;

        case PassCodeAuthModeFail:
            [self passCodeModeEvent:PassCodeAuthModeFail];
            break;

        case PassCodeAuthModeDone:

            [self authDoneAction];

            break;
    }

}

- (void)passCodeModeEvent:(PassCodeMode)passCodeMode {
    if (passCodeMode == PassCodeRegisterModeDone) {
        if ([self.mode isEqualToString:@"REGISTER"]) {
            [self registerDoneAction];
        }
        else {
            [self resetDoneAction];
        }
    }
    else if (passCodeMode == PassCodeChangeModeDone) {
        [self resetDoneAction];
    }
    else if (passCodeMode == PassCodeAuthModeFail) {
        [self authFailAction];
    }
}

- (void)afterAction {
    NSString *inputPassCode = [self.passNumbers componentsJoinedByString:@""];
    if ( _passmode >= PassCodeRegisterMode && _passmode <= PassCodeRegisterModeDone ) {

        if ( _passmode == PassCodeRegisterMode || _passmode == PassCodeRegisterModeNewPassCode || self.firstPassCode == nil ) {
            self.firstPassCode = [[NSString alloc] initWithString:inputPassCode];

            [self updateMode:PassCodeRegisterModeConfirmPassCode];
        }
        else if ( _passmode == PassCodeRegisterModeConfirmPassCode || _passmode == PassCodeRegisterModeWrongConfirmPassCode ) {

            if ( ! [self.firstPassCode isEqualToString:inputPassCode] ) {
                _retryCount += 1;

                if ( _retryCount >= 5 ) {
                    self.firstPassCode = nil;
                    [self clearPassCodes];
                    [self updateMode:PassCodeAuthModeFail];
                    
                    return;
                }

                [self updateMode:PassCodeRegisterModeWrongConfirmPassCode];
            }
            else {
                [[PassCodeManager defaultManager] registerPassCode:inputPassCode];

                [self updateMode:PassCodeRegisterModeDone];
            }
        }

    }
    else if ( _passmode >= PassCodeChangeMode && _passmode <= PassCodeChangeModeDone )
    {
        if ( _passmode == PassCodeChangeMode || _passmode == PassCodeChangeModeNewPassCode || self.firstPassCode == nil ) {
            self.firstPassCode = [[NSString alloc] initWithString:inputPassCode];

            [self updateMode:PassCodeChangeModeConfirmPassCode];
        }
        else if ( _passmode == PassCodeChangeModeConfirmPassCode || _passmode == PassCodeChangeModeWrongConfirmPassCode ) {
            if ( ! [self.firstPassCode isEqualToString:inputPassCode] ) {
                _retryCount += 1;

                if ( _retryCount >= 5 ) {
                    self.firstPassCode = nil;
                    [self clearPassCodes];
                    [self updateMode:PassCodeAuthModeFail];
                    
                    return;
                }
                
                [self updateMode:PassCodeChangeModeWrongConfirmPassCode];
            }
            else {
                [[PassCodeManager defaultManager] registerPassCode:inputPassCode];

                [self updateMode:PassCodeChangeModeDone];
            }
        }
    }
    else if ( _passmode >= PassCodeAuthMode && _passmode <= PassCodeAuthModeDone )
    {
        if ( _passmode == PassCodeAuthMode || _passmode == PassCodeAuthModeInput || _passmode == PassCodeAuthModeWrong ) {

            if ( ! [[PassCodeManager defaultManager] validPassCode:inputPassCode] ) {

                _retryCount += 1;

                if ( _retryCount >= 5 ) {
                    [self updateMode:PassCodeAuthModeFail];

                    return;
                }

                [self updateMode:PassCodeAuthModeWrong];
            }
            else {

                [self updateMode:PassCodeAuthModeDone];
            }
        }
    }
}

- (IBAction)closeButtonClicked:(id)sender {
    NSLog(@"cancel button clicked");
//    [self authFailAction];
    [self dismiss];
}

@end
