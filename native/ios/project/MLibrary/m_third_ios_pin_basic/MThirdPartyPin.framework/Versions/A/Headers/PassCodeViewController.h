//
//  PassCodeViewController.h
//  BAROTALK
//
//  Created by ProgDesigner on 2016. 7. 7..
//
//

#import <UIKit/UIKit.h>
#import "PassCodeManager.h"

@class PassCodeViewController;

@protocol PassCodeViewControllerDelegate <NSObject>

- (void)passCodeDidFinishRegister:(PassCodeViewController *)controller;
- (void)passCodeDidFinishReset:(PassCodeViewController *)controller;
- (void)passCodeDidFinishAuth:(PassCodeViewController *)controller;
- (void)passCodeDidFailAuth:(PassCodeViewController *)controller error:(NSError *)error;

@end

@interface PassCodeViewController : UIViewController
{
    NSInteger _retryCount;
    BOOL _loadedView;
}

@property (nonatomic, weak) id<PassCodeViewControllerDelegate> delegate;

@property (nonatomic, strong) IBOutlet UILabel *labelMessage;
@property (nonatomic, strong) IBOutlet UIView *indicatorView;

@property (nonatomic, strong) NSMutableArray *passNumbers;

@property (nonatomic, strong) NSString *mode;
@property (nonatomic, strong) NSString *firstPassCode;
@property (nonatomic, strong) NSString *secondPassCode;

@property (nonatomic) NSInteger maxCount;
@property (nonatomic) NSInteger codeCount;

@property (nonatomic) BOOL lockedInteraction;

@property (nonatomic, readonly, getter=isPresented) BOOL presented;

- (IBAction)onTouchDownPassButton:(UIButton *)sender;

- (void)loadView;
- (void)showMode:(NSString *)mode;
- (void)setCodeCount:(NSInteger)codeCount;
- (void)setLockedInteraction:(BOOL)lockedInteraction;
- (void)clearPassCodes;
- (void)afterAction;
- (void)startShakingAnimation;
- (void)registerDoneAction;
- (void)resetDoneAction;
- (void)authDoneAction;
- (void)authFailAction;
- (void)authFailAction:(NSString *)msg;
- (void)dismiss;

@end
