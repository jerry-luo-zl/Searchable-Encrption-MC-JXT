#导入数值计算的基础库
import numpy as np 
## 导入画图库
import matplotlib.pyplot as plt
import seaborn as sns
## 导入逻辑回归模型函数
from sklearn.linear_model import LogisticRegression


# 展示原始数据分布
def show_rawdata(x_fearures):
    ## 可视化构造的训练数据
    plt.figure()
    plt.scatter(x_fearures[:,0],x_fearures[:,1], c=y_label, s=50, cmap='viridis')
    plt.title('Dataset')
    plt.show()

#利用numpy随意构造我们想要的数据集及其标签
x_fearures = np.array([[-1, -2], [-2, -1], [-3, -2], [1, 3], [2, 1], [3, 2]])
y_label = np.array([0, 0, 0, 1, 1, 1])
show_rawdata(x_fearures)

# 调用逻辑回归模型
lr_clf = LogisticRegression()
# 用逻辑回归模型拟合构造的数据集
lr_clf = lr_clf.fit(x_fearures, y_label)
print("w0,w1,w2分别为{},{},{}".format(lr_clf.intercept_[0],lr_clf.coef_[0][0],lr_clf.coef_[0][1]))

# 生成两个新的样本
x_fearures_new1 = np.array([[0, -1]])
x_fearures_new2 = np.array([[1, 2]])
# 利用在训练集上训练好的模型进行预测
y_label_new1_predict = lr_clf.predict(x_fearures_new1)
y_label_new2_predict = lr_clf.predict(x_fearures_new2)
# 打印预测结果
print('The New point 1 predict class:\n',y_label_new1_predict)
print('The New point 2 predict class:\n',y_label_new2_predict)

# 由于逻辑回归模型是概率预测模型,所有我们可以利用 predict_proba 函数预测其概率
# predict_proba 函数可以预测样本属于每一类的概率值
y_label_new1_predict_proba = lr_clf.predict_proba(x_fearures_new1)
y_label_new2_predict_proba = lr_clf.predict_proba(x_fearures_new2)

print('The New point 1 predict Probability of each class:\n',y_label_new1_predict_proba)
print('The New point 2 predict Probability of each class:\n',y_label_new2_predict_proba)