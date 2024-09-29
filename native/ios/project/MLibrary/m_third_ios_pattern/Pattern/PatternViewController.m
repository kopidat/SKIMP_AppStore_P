//
//  PatternViewController.m
//  MOBILE_KOREA_MAIL.iOS
//
//  Created by kang on 2017. 9. 14..
//
//

#import "PatternViewController.h"
#import "PatternView.h"
#import "PatternManager.h"
#import <MThirdPartyPattern/MThirdPartyPatternPlugin.h>

typedef NS_ENUM(NSUInteger, PatternState) {
    PatternStateValidation,
    PatternStateEntry,
    PatternStateReEntry
};

@interface PatternViewController () <PatternDelegate>

@property (nonatomic, retain) IBOutlet UIView *lineView;
@property (nonatomic, strong) CAGradientLayer *gradient;

@property (nonatomic, retain) IBOutlet UIView *patternParentView;
@property (nonatomic, strong) PatternView *patternView;

@property (nonatomic, retain) IBOutlet UILabel *titleLabel;
@property (nonatomic, retain) IBOutlet UILabel *infoLabel;
@property (nonatomic, retain) IBOutlet UILabel *messageLabel;

@property (nonatomic) PatternMode patternMode;
@property (nonatomic) PatternState patternState;
@property (nonatomic, strong) NSArray *tempPattern;
@property (nonatomic, weak) id<PatternViewControllerDelegate> delegate;

@property (nonatomic, assign) NSInteger patternFailCount;

@end

@implementation PatternViewController

- (instancetype)initWithMode:(PatternMode)mode delegate:(id<PatternViewControllerDelegate>)delegate {
    if (self = [super init]) {
        self.modalPresentationStyle = UIModalPresentationFullScreen;
        _patternMode = mode;
        _patternState = PatternStateValidation;
        _delegate = delegate;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.view setBackgroundColor:[UIColor whiteColor]];
    
    self.gradient = [CAGradientLayer layer];

    self.gradient.frame = self.lineView.bounds;
    self.gradient.colors = @[(id)[UIColor colorWithHex:@"#FF7A00" alpha:100.0f].CGColor, (id)[UIColor colorWithHex:@"#EA002C" alpha:100.0f].CGColor];

    [self.lineView.layer insertSublayer:self.gradient atIndex:0];
    [self addSubViews];
    [self setLayoutConstraints];
    
    _patternFailCount = 0;
    
    if (self.patternMode == PatternModeModify) {
        self.patternState = PatternStateEntry;
        self.titleLabel.text = @"간편로그인 설정";
        self.infoLabel.text = @"패턴 등록";
    } else if (self.patternMode == PatternModeValidation) {
        self.patternState = PatternStateValidation;
        self.titleLabel.text = @"패턴 인증";
        self.infoLabel.text = @"사용중인 패턴을 그려주세요";
        self.messageLabel.text = @"";
    }
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    
    self.gradient.frame = self.lineView.bounds;
}

- (void)addSubViews {
    [self.patternParentView addSubview:self.patternView];
}

- (void)setLayoutConstraints {    
    NSDictionary *views = @{@"patternView":self.patternView};
    NSDictionary *metrics = @{@"padding":@0};
    NSDictionary *vmetrics = @{@"vPadding":@12};
    [self.patternParentView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"|-(padding)-[patternView]-(padding)-|"
                                                                      options:kNilOptions
                                                                      metrics:metrics
                                                                        views:views]];
    [self.patternParentView addConstraint:[NSLayoutConstraint constraintWithItem:self.patternView
                                                          attribute:NSLayoutAttributeWidth
                                                          relatedBy:NSLayoutRelationEqual
                                                             toItem:self.patternView
                                                          attribute:NSLayoutAttributeHeight
                                                         multiplier:1
                                                           constant:0]];
    [self.patternParentView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|-(vPadding)-[patternView]"
                                                                      options:kNilOptions
                                                                      metrics:vmetrics
                                                                        views:views]];
}

#pragma mark getters
- (PatternView *)patternView {
    if (!_patternView) {
        _patternView = [[PatternView alloc] initWithDelegate:self];
    }
    return _patternView;
}

- (void)setPatternState:(PatternState)patternState {
    _patternState = patternState;
    if (patternState == PatternStateEntry) {
//        [self modifyUIForEntryState];
        self.messageLabel.text = @"4개 이상의 점을 연결하세요";
    } else if (patternState == PatternStateReEntry) {
//        [self modifyUIForReEntryState];
        self.messageLabel.text = @"다시한번 그려주세요";
    }
}

#pragma mark IBActions
- (IBAction)confirmPattern:(id)sender {
    [self updatePatternViewForModificationIsVerified:YES];
}

- (IBAction)cancelModification:(id)sender {
    [[PatternManager sharedManager] deletePattern];
    [self.delegate cancelEditing];
    [self dismissViewControllerAnimated:YES completion:nil];
//    [self.view removeFromSuperview];
}

- (IBAction)resetPattern:(id)sender {
    [self.patternView invalidateCurrentPattern];
//    self.messageLabel.text = @"패턴을 설정해 주세요.";
    self.messageLabel.text = @"4개 이상의 점을 연결해주세요";
}

- (IBAction)continueEditing:(id)sender {
    [self.patternView updateViewForReEntry];
    self.patternState = PatternStateReEntry;
}

#pragma mark Pattern Validation
- (BOOL)isPatternCorrect:(NSArray *)pattern {
    if (self.patternState == PatternStateValidation) {
        return [[PatternManager sharedManager] isCorrectPattern:pattern];
    } else if (self.patternState == PatternStateReEntry) {
        return [[PatternManager sharedManager] pattern:pattern isEqualToPattern:self.tempPattern];
    }
    return YES;
}

- (BOOL)isPatternValid:(NSArray *)pattern {
    return [pattern count] >= 5;
}

#pragma mark Pattern Delegate
- (void)enteredPattern:(NSArray *)pattern {
    if (self.patternState == PatternStateEntry) {
        if ([self isPatternValid:pattern]) {
            self.tempPattern = [pattern copy];
            self.messageLabel.text = @"패턴을 저장하였습니다.";
            [self.patternView updateViewForReEntry];
            self.patternState = PatternStateReEntry;
        } else {
            [self showInvalidPatternAlert];
            [self.patternView updateViewForReEntry];
        }
    } else {
        if ([self isPatternCorrect:pattern]) {
            if (self.patternMode == PatternModeValidation) {
                [self.patternView updateViewForCorrectPatternAnimates:YES];
            } else if (self.patternMode == PatternModeModify && self.patternState == PatternStateValidation) {
                self.patternState = PatternStateEntry;
                [self updatePatternViewForModificationIsVerified:YES];
            } else if (self.patternState == PatternStateReEntry) {
                self.messageLabel.text = @"아래와 같은 잠금해제 패턴을 설정하였습니다.";
                [self updatePatternViewForModificationIsVerified:YES];
            }
        } else {
            if (self.patternMode == PatternModeValidation) {
                [self patternResultFail];
            } else {
                [self patternResultFail];
            }
            [self.patternView updateViewForInCorrectPattern];
        }
    }
}

- (void)patternResultFail {
    self.patternFailCount++;
    if (self.patternFailCount < 5) {
        self.messageLabel.text = [NSString stringWithFormat:@"일치하지 않습니다 (%ld/5)", self.patternFailCount];
    } else {
        [[PatternManager sharedManager] deletePattern];
        [self.delegate failedPattern];
        //[self.view removeFromSuperview];
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}

#pragma mark update Pattern View
- (void)updateViewForCorrectPattern {
    switch (self.patternMode) {
        case PatternModeValidation:
            [self.patternView updateViewForCorrectPatternAnimates:YES];
            break;
        case PatternModeModify:
            [self.patternView updateViewForInCorrectPattern];
            break;
        default:
            break;
    }
}

- (void)updatePatternViewForModificationIsVerified:(BOOL)verified {
    
    if (verified) {
        [self.patternView updateViewForCorrectPatternAnimates:NO];
        if (self.patternMode == PatternModeValidation) {
            [self.patternView updateViewForCorrectPatternAnimates:YES];
        } else {
            if (self.patternState == PatternStateReEntry) {
                [self updateStoredPattern];
                [self.delegate modifiedPattern];
                //[self.view removeFromSuperview];
                [self dismissViewControllerAnimated:YES completion:nil];
            }
        }
    } else {
        [self updatePatternViewForModificationIsVerified:NO];
    }
}

- (void)updateStoredPattern {
    [[PatternManager sharedManager] updatePattern:self.tempPattern];
}

- (void)showInvalidPatternAlert {
    [self.messageLabel setText:@"패턴이 너무 단순합니다.\n다시 시도해 주세요."];
}

- (void)updateUIForPatternEntry {
    
}

#pragma mark Pattern View Delegate
- (void)completedAnimations {
    [self.delegate unlockedPattern];
    //[self.view removeFromSuperview];
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)startedDrawing {
    if (self.patternState == PatternStateEntry || self.patternState == PatternStateReEntry) {
        [self.messageLabel setText:@"패턴 입력이 완료되면 손가락을 떼세요."];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
